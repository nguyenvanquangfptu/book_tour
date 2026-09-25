# Prefix list AWS-managed chua toan bo dai IP "origin-facing" cua CloudFront - dung de
# chi cho phep CloudFront goi thang vao backend, chan moi truy cap truc tiep tu Internet.
data "aws_ec2_managed_prefix_list" "cloudfront" {
  name = "com.amazonaws.global.cloudfront.origin-facing"
}

resource "aws_security_group" "ec2" {
  name        = "booktour-ec2-sg"
  description = "Khong mo port quan tri nao ra ngoai (dung SSM Session Manager); cong backend chi nhan traffic tu CloudFront"
  vpc_id      = aws_vpc.main.id

  # Khong co ingress cho port 22: quan tri EC2 qua SSM Session Manager (infra/ssm.tf),
  # khong can mo SSH ra Internet.

  ingress {
    description     = "Backend API - chi CloudFront (api-cdn) duoc goi vao, khong public truc tiep"
    from_port       = 8082
    to_port         = 8082
    protocol        = "tcp"
    prefix_list_ids = [data.aws_ec2_managed_prefix_list.cloudfront.id]
  }

  egress {
    description = "Cho phep moi ket noi ra ngoai (pull image, apt update...)"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "booktour-ec2-sg" }
}
