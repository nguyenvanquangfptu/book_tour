# Cho phep GitLab CI (job deploy-frontend) tu doi lay quyen tam thoi qua IAM Role thay vi
# luu AWS access key/secret lam CI variable.
data "tls_certificate" "gitlab" {
  url = "https://gitlab.com"
}

resource "aws_iam_openid_connect_provider" "gitlab" {
  url             = "https://gitlab.com"
  client_id_list  = ["https://gitlab.com"]
  thumbprint_list = [data.tls_certificate.gitlab.certificates[0].sha1_fingerprint]
}

resource "aws_iam_role" "gitlab_ci_deploy" {
  name = "gitlab-ci-frontend-deploy"

  # Chi cho phep token OIDC phat hanh cho dung project GitLab nay VA dung nhanh main assume role.
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Federated = aws_iam_openid_connect_provider.gitlab.arn }
      Action    = "sts:AssumeRoleWithWebIdentity"
      Condition = {
        StringEquals = {
          "gitlab.com:aud" = "https://gitlab.com"
        }
        StringLike = {
          "gitlab.com:sub" = "project_path:${var.gitlab_project_path}:ref_type:branch:ref:main"
        }
      }
    }]
  })

  tags = { Name = "booktour-gitlab-ci-deploy" }
}

# Quyen toi thieu: chi ghi vao dung bucket frontend va invalidate dung 1 CloudFront distribution.
# Khong dung gi toi EC2/backend - deploy backend chay tren self-hosted runner, khong qua vai tro nay.
resource "aws_iam_role_policy" "gitlab_ci_deploy" {
  name = "gitlab-ci-frontend-deploy-policy"
  role = aws_iam_role.gitlab_ci_deploy.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "S3FrontendBucketOnly"
        Effect   = "Allow"
        Action   = ["s3:PutObject", "s3:DeleteObject", "s3:ListBucket"]
        Resource = [aws_s3_bucket.frontend.arn, "${aws_s3_bucket.frontend.arn}/*"]
      },
      {
        Sid      = "InvalidateFrontendDistributionOnly"
        Effect   = "Allow"
        Action   = ["cloudfront:CreateInvalidation"]
        Resource = aws_cloudfront_distribution.frontend.arn
      }
    ]
  })
}
