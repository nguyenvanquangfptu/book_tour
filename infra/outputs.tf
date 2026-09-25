output "elastic_ip" {
  description = "Public IP co dinh cua EC2 backend"
  value       = aws_eip.backend.public_ip
}

output "frontend_bucket_name" {
  value = aws_s3_bucket.frontend.bucket
}

output "frontend_cloudfront_domain" {
  description = "Domain *.cloudfront.net cua frontend - dung lam target cho ban ghi CNAME www"
  value       = aws_cloudfront_distribution.frontend.domain_name
}

output "frontend_cloudfront_distribution_id" {
  value = aws_cloudfront_distribution.frontend.id
}

output "api_cloudfront_domain" {
  description = "Domain *.cloudfront.net cua API - dung lam target cho ban ghi CNAME api"
  value       = aws_cloudfront_distribution.api.domain_name
}

output "api_cloudfront_distribution_id" {
  value = aws_cloudfront_distribution.api.id
}

output "gitlab_ci_role_arn" {
  description = "Dan vao GitLab CI/CD variable AWS_ROLE_ARN"
  value       = aws_iam_role.gitlab_ci_deploy.arn
}
