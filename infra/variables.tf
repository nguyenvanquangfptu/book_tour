variable "aws_region" {
  description = "AWS region cho EC2/VPC/S3."
  type        = string
  default     = "ap-southeast-1"
}

variable "budget_email" {
  description = "Email nhan canh bao AWS Budget"
  type        = string
}

variable "gitlab_project_path" {
  description = "Duong dan project GitLab, dang namespace/project, dung de gioi han IAM Role OIDC chi cho nhanh main cua dung project nay"
  type        = string
  default     = "personal-group4175339/booking_tour"
}

variable "instance_type" {
  description = "Loai EC2 - giu t3.micro de nam trong Free Tier"
  type        = string
  default     = "t3.micro"
}
