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

  # State luu tren S3, khong phai file cuc bo - bat buoc vi terraform apply chay tren runner
  # GitLab, moi lan job la 1 moi truong sach. Dung lai AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY
  # da co san (khong can them token/bien nao khac). Cac gia tri thuc te (bucket/key/region)
  # duoc truyen qua "terraform init -backend-config=..." trong .gitlab-ci.yml. Bucket phai
  # duoc tao truoc 1 lan qua job "bootstrap-state-bucket".
  backend "s3" {}
}

provider "aws" {
  region = var.aws_region
}
