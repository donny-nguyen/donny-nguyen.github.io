# Terraform

**Terraform** is an open-source Infrastructure as Code (IaC) tool created by HashiCorp that lets you define, provision, and manage infrastructure across many providers using a declarative configuration language. Because it is cloud-agnostic, the same workflow works for AWS, Azure, Google Cloud, Kubernetes, and hundreds of other services through a large ecosystem of providers.

## Core Concepts

- **Providers**: Plugins that let Terraform interact with a platform's API (for example, the `aws`, `azurerm`, or `google` provider). Each provider exposes resources and data sources.
- **Resources**: The infrastructure components you manage, such as an EC2 instance, an S3 bucket, or a DNS record.
- **Data Sources**: Read-only lookups that fetch information about existing infrastructure to use in your configuration.
- **State**: A file (`terraform.tfstate`) that maps your configuration to real-world resources. Terraform uses it to detect drift and plan changes.
- **Modules**: Reusable, encapsulated groups of resources that promote consistency and reduce duplication.
- **Variables and Outputs**: Inputs that parameterize configurations and outputs that expose useful values after an apply.

## HashiCorp Configuration Language (HCL)

Terraform configurations are written in **HCL**, a declarative language designed to be human-readable. You describe *what* you want, and Terraform determines *how* to achieve it, including the correct order of operations based on resource dependencies.

```hcl
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

resource "aws_s3_bucket" "app_bucket" {
  bucket = "my-application-bucket"

  tags = {
    Environment = "Production"
    ManagedBy   = "Terraform"
  }
}

output "bucket_arn" {
  value = aws_s3_bucket.app_bucket.arn
}
```

## The Terraform Workflow

```bash
terraform init      # Initialize the working directory and download providers
terraform fmt       # Format configuration files consistently
terraform validate  # Check the configuration for syntax and internal errors
terraform plan      # Preview the changes Terraform will make
terraform apply     # Apply the changes to reach the desired state
terraform destroy   # Remove all resources managed by the configuration
```

The key step is `terraform plan`, which produces an execution plan showing exactly what will be created, changed, or destroyed before anything happens.

## State Management

Terraform's state file is central to how it works. For teams, storing state locally is risky, so Terraform supports **remote backends** that keep state in a shared, secure location with locking to prevent concurrent modifications.

A common AWS backend uses an S3 bucket for state storage and a DynamoDB table for locking:

```hcl
terraform {
  backend "s3" {
    bucket         = "my-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}
```

## Modules for Reusability

Modules let you package a set of resources and reuse them across environments and projects.

```hcl
module "network" {
  source     = "./modules/vpc"
  cidr_block = "10.0.0.0/16"
  environment = "production"
}
```

You can also consume modules from the public **Terraform Registry**, which hosts thousands of community and verified modules.

## Advantages

- **Cloud-agnostic**: One tool and workflow for many providers.
- **Large ecosystem**: Extensive provider and module registry.
- **Explicit execution plans**: `plan` shows changes before they are applied.
- **Strong community**: Widely adopted with abundant documentation and examples.

## Considerations

- **State management overhead**: State files must be stored securely and protected, especially in teams.
- **Drift**: Manual changes made outside Terraform can cause drift that must be reconciled.
- **Learning curve**: HCL and state concepts take time to master.

## Conclusion

Terraform is one of the most popular IaC tools because of its declarative approach, cloud-agnostic design, and rich ecosystem. By defining infrastructure in HCL, managing state carefully, and following a consistent plan-and-apply workflow, teams can provision and evolve infrastructure reliably across virtually any platform.
