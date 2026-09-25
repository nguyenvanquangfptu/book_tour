# CloudFront thu 2, dung lam TLS termination mien phi truoc EC2 - tranh phai tu chay
# Certbot/Nginx tren EC2, va cho frontend (CloudFront) goi API khong bi chan mixed-content.
# Chua gan domain rieng - dung thang domain *.cloudfront.net (van co HTTPS mien phi).
resource "aws_cloudfront_distribution" "api" {
  enabled     = true
  price_class = "PriceClass_100"

  origin {
    domain_name = aws_eip.backend.public_ip
    origin_id   = "ec2-backend"

    custom_origin_config {
      http_port              = 8082
      https_port              = 443
      origin_protocol_policy  = "http-only" # CloudFront <-> EC2 la HTTP; viewer <-> CloudFront moi can HTTPS
      origin_ssl_protocols    = ["TLSv1.2"]
    }
  }

  default_cache_behavior {
    allowed_methods           = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods             = ["GET", "HEAD"]
    target_origin_id           = "ec2-backend"
    viewer_protocol_policy     = "redirect-to-https"
    cache_policy_id            = data.aws_cloudfront_cache_policy.caching_disabled.id
    origin_request_policy_id   = data.aws_cloudfront_origin_request_policy.all_viewer_except_host.id
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = { Name = "booktour-api-cdn" }
}
