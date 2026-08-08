# AWS CloudFormation

**AWS CloudFormation** is Amazon's native Infrastructure as Code (IaC) service. It lets you model, provision, and manage AWS (and some third-party) resources by describing them in a template. CloudFormation reads the template, works out the correct order to create resources based on their dependencies, and provisions them as a single, manageable unit called a **stack**.

## Core Concepts

- **Template**: A JSON or YAML file that declares the resources and their configuration.
- **Stack**: A collection of AWS resources created and managed together from a single template. Creating, updating, or deleting a stack acts on all its resources.
- **Change Set**: A preview of the changes CloudFormation will make to a stack before you execute them, similar to a plan.
- **Stack Set**: A way to deploy the same stack across multiple AWS accounts and regions from a single template.
- **Drift Detection**: A built-in feature that identifies when resources have been changed outside of CloudFormation.

## Template Structure

A CloudFormation template is organized into several sections, most of which are optional except `Resources`:

- **AWSTemplateFormatVersion**: The template format version.
- **Description**: A text description of the template.
- **Parameters**: Input values you supply at deployment time.
- **Mappings**: Fixed key-value lookups, often used for region-specific values.
- **Conditions**: Rules that control whether resources are created.
- **Resources** (required): The AWS resources to provision.
- **Outputs**: Values returned after the stack is created, such as an endpoint URL.

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Description: Provision an S3 bucket with configurable environment

Parameters:
  EnvironmentName:
    Type: String
    Default: Production
    AllowedValues:
      - Development
      - Staging
      - Production

Resources:
  AppBucket:
    Type: AWS::S3::Bucket
    Properties:
      BucketName: my-application-bucket
      Tags:
        - Key: Environment
          Value: !Ref EnvironmentName
        - Key: ManagedBy
          Value: CloudFormation

Outputs:
  BucketArn:
    Description: The ARN of the created bucket
    Value: !GetAtt AppBucket.Arn
```

## Intrinsic Functions

CloudFormation provides **intrinsic functions** to add dynamic behavior to templates:

- `!Ref` – Reference a parameter or resource.
- `!GetAtt` – Retrieve an attribute of a resource (for example, an ARN).
- `!Sub` – Substitute variables into a string.
- `!Join` – Concatenate values with a delimiter.
- `!FindInMap` – Look up a value in a mapping.

## The CloudFormation Workflow

You can deploy templates through the AWS Management Console, the AWS CLI, or CI/CD pipelines.

```bash
# Preview changes with a change set
aws cloudformation create-change-set \
  --stack-name my-app-stack \
  --template-body file://template.yaml \
  --change-set-name my-changes

# Deploy or update a stack
aws cloudformation deploy \
  --stack-name my-app-stack \
  --template-file template.yaml \
  --parameter-overrides EnvironmentName=Production

# Delete a stack and all its resources
aws cloudformation delete-stack --stack-name my-app-stack
```

## Advantages

- **Native AWS integration**: First-class support for AWS services, often the first to support new features.
- **No extra tooling or state files**: AWS manages the state of your stacks for you.
- **Automatic rollback**: If a stack update fails, CloudFormation can roll back to the last known good state.
- **Stack Sets**: Deploy consistently across many accounts and regions.
- **Drift detection**: Built-in detection of out-of-band changes.

## Considerations

- **AWS-only**: CloudFormation is designed for AWS and is not cloud-agnostic.
- **Verbosity**: Large templates in JSON or YAML can become long and hard to manage.
- **Limited logic**: Templates support conditions and functions but are not a full programming language (this is where AWS CDK helps).

## Conclusion

AWS CloudFormation is a powerful, fully managed IaC service for teams committed to AWS. By declaring resources in templates and managing them as stacks, you get reliable provisioning, automatic state management, rollback on failure, and multi-account deployment—all without maintaining separate state infrastructure.
