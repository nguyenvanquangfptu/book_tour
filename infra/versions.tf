terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.0"
    }
  }

  # State luu tren GitLab (GitLab-managed Terraform state), khong phai file cuc bo -
  # bat buoc vi terraform apply chay tren runner GitLab, moi lan job la 1 moi truong sach.
  # Cac gia tri thuc te duoc truyen qua "terraform init -backend-config=..." trong .gitlab-ci.yml.
  backend "http" {}
}

provider "aws" {
  region = var.aws_region
}
