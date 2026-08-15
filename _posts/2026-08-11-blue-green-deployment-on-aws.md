# Deploying an Application on AWS Using Blue-Green Deployment

**Blue-Green Deployment** on AWS lets you release a new version of your application with near-zero downtime and an instant rollback path. You run two identical environments, **Blue** (current) and **Green** (new), and shift production traffic from one to the other only after the new version is fully verified. AWS provides several managed services that make this pattern straightforward to implement.

For a conceptual overview of the strategy, see [Blue-Green Deployment](https://donny-nguyen.github.io/2026/08/11/blue-green-deployment.html).

## Choosing the Right AWS Approach

AWS offers multiple ways to implement blue-green deployment depending on where your application runs:

| Platform | Blue-Green Mechanism |
| --- | --- |
| **Amazon ECS / AWS Fargate** | CodeDeploy shifts traffic between two target groups behind an ALB |
| **AWS Elastic Beanstalk** | Swap environment URLs (CNAME swap) |
| **Amazon EC2** | CodeDeploy provisions a replacement fleet and reroutes the load balancer |
| **AWS Lambda** | CodeDeploy shifts traffic between function versions using aliases |
| **Amazon EKS (Kubernetes)** | Two deployments/services with traffic shifted via ingress or service mesh |

The core building blocks are usually an **Application Load Balancer (ALB)**, **target groups**, and **AWS CodeDeploy** to orchestrate the traffic shift.

## Option 1: Blue-Green on Amazon ECS with CodeDeploy

This is one of the most common and fully managed approaches.

### Architecture

- An **Application Load Balancer** with a **production listener** (port 443) and an optional **test listener** (port 8443).
- Two **target groups**: `blue-tg` and `green-tg`.
- An **ECS service** configured with the `CODE_DEPLOY` deployment controller.
- An **AWS CodeDeploy** application and deployment group that manage the traffic shift.

### Steps

1. **Create two target groups** (`blue-tg` and `green-tg`) registered with your ALB.

2. **Configure the ECS service** to use the CodeDeploy deployment controller:

   ```json
   {
     "serviceName": "my-app-service",
     "deploymentController": { "type": "CODE_DEPLOY" },
     "loadBalancers": [
       {
         "targetGroupArn": "arn:aws:elasticloadbalancing:...:targetgroup/blue-tg/...",
         "containerName": "my-app",
         "containerPort": 8080
       }
     ]
   }
   ```

3. **Define the CodeDeploy `AppSpec` file** describing the new task definition:

   ```yaml
   version: 0.0
   Resources:
     - TargetService:
         Type: AWS::ECS::Service
         Properties:
           TaskDefinition: <TASK_DEFINITION_ARN>
           LoadBalancerInfo:
             ContainerName: "my-app"
             ContainerPort: 8080
   ```

4. **Create a CodeDeploy deployment group** with a blue-green configuration. Choose a traffic-shifting policy:
   - `CodeDeployDefault.ECSAllAtOnce` — shift all traffic immediately.
   - `CodeDeployDefault.ECSLinear10PercentEvery1Minute` — shift gradually.
   - `CodeDeployDefault.ECSCanary10Percent5Minutes` — canary-style shift.

5. **Trigger the deployment**. CodeDeploy launches a new task set (Green) behind the `green-tg`, runs health checks, and then reroutes the ALB production listener from `blue-tg` to `green-tg`.

6. **Validate and finalize**. After a configurable bake time, CodeDeploy terminates the old (Blue) task set. If health checks or CloudWatch alarms fail, it automatically rolls back to Blue.

## Option 2: Blue-Green on AWS Elastic Beanstalk

Elastic Beanstalk uses a **CNAME swap** to achieve blue-green deployment.

1. **Deploy the current version** to the Blue environment (for example, `my-app-blue`).
2. **Clone the environment** or create a new one, and **deploy the new version** to the Green environment (`my-app-green`).
3. **Test the Green environment** using its own URL.
4. **Swap environment URLs** from the Elastic Beanstalk console or CLI:

   ```bash
   aws elasticbeanstalk swap-environment-cnames \
     --source-environment-name my-app-blue \
     --destination-environment-name my-app-green
   ```

5. **Monitor** the newly promoted environment. To roll back, simply swap the CNAMEs again.

Because the swap changes DNS, keep the old environment running until DNS caches have expired and you are confident in the new release.

## Option 3: Blue-Green for AWS Lambda

CodeDeploy shifts traffic between two versions of a Lambda function using an **alias**.

1. **Publish a new version** of the function.
2. **Point an alias** (for example, `prod`) at the current version.
3. **Configure CodeDeploy** to shift the alias traffic to the new version using a policy such as `Linear`, `Canary`, or `AllAtOnce`.
4. **Use pre- and post-traffic hooks** (Lambda functions) to run validation before and after the shift.
5. CodeDeploy monitors CloudWatch alarms and **rolls back automatically** if the new version fails.

See also [Invoke Lambda Aliases Using Stage Variables](https://donny-nguyen.github.io/2026/07/11/invoke-lambda-aliases-using-stage-variables.html).

## Handling the Database

Both environments usually share the same database (Amazon RDS or DynamoDB), so schema changes must be compatible with both versions:

- **Use backward-compatible migrations**: add new columns or tables before switching traffic.
- **Follow the expand-and-contract pattern**: expand the schema, deploy new code, switch traffic, then contract (remove old columns) after Blue is retired.
- **Avoid destructive changes** in the same release that performs the switch.

## Automating with CI/CD

Wire blue-green deployment into a pipeline for repeatable releases:

- **AWS CodePipeline** orchestrates the stages: source, build, and deploy.
- **AWS CodeBuild** builds the container image or artifact and pushes it to **Amazon ECR** or **S3**.
- **AWS CodeDeploy** performs the blue-green traffic shift.
- **Amazon CloudWatch alarms** act as rollback triggers if error rates or latency spike after the switch.

## Best Practices on AWS

- **Enable automatic rollback** in CodeDeploy tied to CloudWatch alarms.
- **Set an appropriate bake time** before terminating the old environment.
- **Configure health checks** on target groups so unhealthy tasks never receive production traffic.
- **Use a separate test listener** to validate Green before the production switch.
- **Right-size resources**, since running two environments temporarily doubles cost.
- **Tag environments** clearly (`blue` / `green`) for observability and cost tracking.
- **Combine with feature flags** to decouple deployment from feature release.

## Conclusion

AWS makes blue-green deployment practical through managed services like **CodeDeploy**, **Elastic Beanstalk**, and **Lambda aliases**, all coordinated by an **Application Load Balancer** and integrated with **CloudWatch** for automatic rollback. By deploying the new version to an idle environment, validating it, and shifting traffic only when it is healthy, you achieve reliable, low-risk releases with minimal downtime and a fast path back if anything goes wrong.
