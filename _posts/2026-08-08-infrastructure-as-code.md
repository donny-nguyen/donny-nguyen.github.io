# Infrastructure as Code (IaC)

**Infrastructure as Code (IaC)** is the practice of provisioning and managing infrastructure—servers, networks, databases, load balancers, and more—through machine-readable configuration files rather than manual processes or interactive tools. Instead of clicking through a cloud console, you describe the desired state of your infrastructure in code, then let a tool create, update, or destroy resources to match that description.

## Why IaC Matters

Manually configuring infrastructure is slow, error-prone, and hard to reproduce. IaC solves these problems by treating infrastructure the same way we treat application source code:

- **Consistency**: The same configuration produces the same environment every time, eliminating "works on my machine" issues across development, staging, and production.
- **Version Control**: Infrastructure definitions live in Git, giving you history, code review, rollback, and collaboration.
- **Speed and Automation**: Entire environments can be provisioned in minutes and integrated into CI/CD pipelines.
- **Documentation**: The code itself serves as always-up-to-date documentation of what infrastructure exists and how it is configured.
- **Cost Control**: Environments can be spun up on demand and torn down when no longer needed.

## Declarative vs. Imperative Approaches

There are two main styles of IaC:

- **Declarative** (the "what"): You describe the desired end state, and the tool figures out how to reach it. Terraform, AWS CloudFormation, and Pulumi (in most usage) are declarative.
- **Imperative** (the "how"): You specify the exact commands and steps needed to reach the desired state. Scripts using the AWS CLI or Ansible playbooks often lean imperative.

Declarative tools are generally preferred because they let the tool handle dependency ordering, detect drift, and reconcile the current state with the desired state.

## Key Concepts

1. **Desired State**: The target configuration you define in code.
2. **State Management**: Many tools (like Terraform) keep a state file that maps your configuration to real-world resources, enabling them to detect changes and plan updates.
3. **Idempotency**: Applying the same configuration multiple times yields the same result without unintended side effects.
4. **Drift Detection**: Identifying when the real infrastructure has diverged from the code (for example, due to a manual change in the console).
5. **Modules / Reusability**: Packaging configuration into reusable components to avoid duplication across environments and projects.

## Common IaC Tools

| Tool | Provider | Language / Format | Notes |
| --- | --- | --- | --- |
| **Terraform** | HashiCorp | HCL | Cloud-agnostic, huge provider ecosystem, declarative. |
| **AWS CloudFormation** | AWS | YAML / JSON | Native AWS service, tight integration with AWS. |
| **AWS CDK** | AWS | TypeScript, Python, Java, etc. | Define infrastructure using familiar programming languages; synthesizes to CloudFormation. |
| **Pulumi** | Pulumi | TypeScript, Python, Go, etc. | Multi-cloud, uses general-purpose languages. |
| **Ansible** | Red Hat | YAML | Configuration management and provisioning, often imperative. |
| **Bicep** | Microsoft | Bicep DSL | Simplified language for Azure Resource Manager. |

## Example: Terraform

A simple Terraform configuration that provisions an AWS S3 bucket:

```hcl
provider "aws" {
  region = "us-east-1"
}

resource "aws_s3_bucket" "app_bucket" {
  bucket = "my-application-bucket"

  tags = {
    Environment = "Production"
    ManagedBy   = "Terraform"
  }
}
```

The typical workflow is:

```bash
terraform init      # Download providers and set up the working directory
terraform plan      # Preview the changes without applying them
terraform apply     # Create or update resources to match the configuration
terraform destroy   # Tear down all managed resources
```

## Example: AWS CloudFormation

The same S3 bucket described in CloudFormation YAML:

```yaml
Resources:
  AppBucket:
    Type: AWS::S3::Bucket
    Properties:
      BucketName: my-application-bucket
      Tags:
        - Key: Environment
          Value: Production
        - Key: ManagedBy
          Value: CloudFormation
```

## Best Practices

- **Store everything in version control** and require code reviews for infrastructure changes.
- **Use modules and reusable components** to keep configurations DRY.
- **Separate environments** (dev, staging, production) using workspaces, directories, or parameter files.
- **Never make manual changes** in the console for IaC-managed resources; always change the code to avoid drift.
- **Protect and back up state files**, and use remote state with locking (for example, an S3 bucket with DynamoDB locking for Terraform) to support team collaboration.
- **Keep secrets out of code**, using a secrets manager or encrypted variables instead of hardcoding credentials.
- **Run IaC in CI/CD** so `plan` and `apply` steps are automated, auditable, and consistent.

## Conclusion

Infrastructure as Code transforms infrastructure management from a manual, error-prone chore into a repeatable, automated, and collaborative engineering discipline. By defining infrastructure declaratively, versioning it, and integrating it into automated pipelines, teams gain reliability, speed, and confidence when building and scaling modern cloud systems.
