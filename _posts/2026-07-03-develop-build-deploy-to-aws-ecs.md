# Typical Workflow to Develop, Build, and Deploy an Application to AWS ECS with Docker

Amazon Elastic Container Service (ECS) is a fully managed container orchestration service that makes it easy to run, scale, and secure Docker containers on AWS. This article walks through a typical end-to-end workflow: from developing your application locally with Docker, to building and pushing an image, to deploying it on ECS.

## Overview of the Workflow

At a high level, the journey from code to production on ECS looks like this:

1. **Develop** the application and write a `Dockerfile`.
2. **Build and test** the Docker image locally.
3. **Push** the image to a container registry (Amazon ECR).
4. **Define** the infrastructure: cluster, task definition, and service.
5. **Deploy** the service to ECS (on Fargate or EC2).
6. **Monitor, update, and roll back** as needed.

Let's go through each step.

## 1. Develop the Application Locally

Start by building your application as you normally would. The key difference is that you package it as a container so it runs the same way everywhere.

### Write a Dockerfile

Create a `Dockerfile` that describes how to build the image. A multi-stage build keeps the final image small and secure. Here's an example for a Node.js application:

```dockerfile
# ---- Build stage ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# ---- Runtime stage ----
FROM node:20-alpine
WORKDIR /app
ENV NODE_ENV=production
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/dist ./dist
COPY package*.json ./
EXPOSE 3000
CMD ["node", "dist/server.js"]
```

### Best Practices at This Stage

- Use a **`.dockerignore`** file to exclude `node_modules`, `.git`, and other unneeded files.
- Keep containers **stateless**; store data in external services (RDS, DynamoDB, S3).
- Read configuration from **environment variables** rather than hardcoding values.
- Pin base image versions for reproducible builds.

## 2. Build and Test the Image Locally

Build the image and run it locally to verify everything works before pushing to the cloud.

```bash
# Build the image
docker build -t my-app:latest .

# Run the container locally
docker run -p 3000:3000 --env-file .env my-app:latest
```

Visit `http://localhost:3000` (or the relevant port) to confirm the application behaves as expected. Using **Docker Compose** is helpful when your app depends on other services (a database, cache, etc.) during local development.

## 3. Push the Image to Amazon ECR

Amazon Elastic Container Registry (ECR) is a managed Docker registry that integrates tightly with ECS.

### Create a Repository

```bash
aws ecr create-repository --repository-name my-app --region us-east-1
```

### Authenticate, Tag, and Push

```bash
# Authenticate Docker to your ECR registry
aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin \
    <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Tag the image with the ECR repository URI
docker tag my-app:latest \
  <account-id>.dkr.ecr.us-east-1.amazonaws.com/my-app:latest

# Push the image
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/my-app:latest
```

Use meaningful image tags (for example, a Git commit SHA or a build number) instead of always relying on `latest`, so you can trace exactly which version is running and roll back precisely.

## 4. Define the ECS Infrastructure

ECS deployment involves three main building blocks:

### Cluster

A **cluster** is a logical grouping of compute capacity where your tasks run. With **AWS Fargate**, you don't manage servers at all—AWS provisions the compute for you. With the **EC2 launch type**, you manage a fleet of EC2 instances.

```bash
aws ecs create-cluster --cluster-name my-app-cluster
```

### Task Definition

A **task definition** is a JSON blueprint that describes how to run your container: which image to use, CPU and memory, port mappings, environment variables, and logging configuration.

```json
{
  "family": "my-app-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "executionRoleArn": "arn:aws:iam::<account-id>:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "my-app",
      "image": "<account-id>.dkr.ecr.us-east-1.amazonaws.com/my-app:latest",
      "portMappings": [
        { "containerPort": 3000, "protocol": "tcp" }
      ],
      "essential": true,
      "environment": [
        { "name": "NODE_ENV", "value": "production" }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/my-app",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

Register it with:

```bash
aws ecs register-task-definition --cli-input-json file://task-definition.json
```

### Service

A **service** keeps a specified number of task instances running and can register them behind an **Application Load Balancer (ALB)** for high availability. It automatically replaces failed tasks and enables rolling updates.

```bash
aws ecs create-service \
  --cluster my-app-cluster \
  --service-name my-app-service \
  --task-definition my-app-task \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-abc123],securityGroups=[sg-abc123],assignPublicIp=ENABLED}"
```

## 5. Deploy and Update

Once the service is created, ECS pulls the image from ECR and starts your tasks. To deploy a new version:

1. Build and push a new image with a fresh tag.
2. Register a new revision of the task definition pointing to that image.
3. Update the service to use the new revision.

```bash
aws ecs update-service \
  --cluster my-app-cluster \
  --service-name my-app-service \
  --task-definition my-app-task \
  --force-new-deployment
```

ECS performs a **rolling deployment** by default, gradually replacing old tasks with new ones while keeping the service available. You can configure the minimum healthy percent and maximum percent to control how aggressive the rollout is.

## 6. Monitor, Scale, and Roll Back

- **Monitoring**: Use **Amazon CloudWatch** for logs and metrics (CPU, memory, request counts). Set alarms on key thresholds.
- **Auto Scaling**: Configure **Service Auto Scaling** to adjust the desired task count based on CPU/memory utilization or custom metrics.
- **Rollback**: If a deployment fails health checks, ECS can automatically roll back when **deployment circuit breaker** is enabled, or you can update the service back to a previous task definition revision.

## Automating with CI/CD

In practice, steps 2 through 5 are automated in a **CI/CD pipeline** (for example, GitHub Actions, GitLab CI, or AWS CodePipeline with CodeBuild). A typical pipeline:

1. Runs tests on every commit.
2. Builds the Docker image and tags it with the commit SHA.
3. Pushes the image to ECR.
4. Registers a new task definition revision.
5. Updates the ECS service to trigger a rolling deployment.

This gives you fast, repeatable, and auditable deployments with minimal manual intervention.

## Summary

The typical workflow for deploying to AWS ECS with Docker follows a clear path: **develop and containerize** your app with a `Dockerfile`, **build and test** the image locally, **push** it to ECR, **define** the ECS cluster, task definition, and service, then **deploy and monitor** it—ideally through an automated CI/CD pipeline. With Fargate, you get all of this without managing any servers, letting you focus on your application while AWS handles the underlying infrastructure.
