data "aws_caller_identity" "current" {}

# Bucket rieng tu hoan toan - khong bao gio public, chi CloudFront (qua OAC) duoc doc.
resource "aws_s3_bucket" "frontend" {
  bucket = "booktour-frontend-${data.aws_caller_identity.current.account_id}"

  tags = { Name = "booktour-frontend" }
}

resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_cloudfront_origin_access_control" "frontend" {
  name                              = "booktour-frontend-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "frontend" {
  enabled             = true
  default_root_object = "index.html"
  # Chua gan domain rieng (chua truy cap duoc DNS) - dung thang domain *.cloudfront.net,
  # van co HTTPS mien phi qua chung chi mac dinh cua CloudFront (xem viewer_certificate).
  price_class         = "PriceClass_100" # re nhat (Bac My + Chau Au), du cho thuc hanh

  origin {
    domain_name              = aws_s3_bucket.frontend.bucket_regional_domain_name
    origin_id                = "s3-frontend"
    origin_access_control_id = aws_cloudfront_origin_access_control.frontend.id
  }

  default_cache_behavior {
    allowed_methods         = ["GET", "HEAD"]
    cached_methods          = ["GET", "HEAD"]
    target_origin_id        = "s3-frontend"
    viewer_protocol_policy  = "redirect-to-https"
    cache_policy_id         = data.aws_cloudfront_cache_policy.caching_optimized.id
    compress                = true
  }

  # SPA routing: React Router xu ly route o client, nen moi duong dan khong khop file
  # tinh (403/404 tu S3) can tra ve index.html voi status 200.
  custom_error_response {
    error_code         = 403
    response_code      = 200
    response_page_path = "/index.html"
  }
  custom_error_response {
    error_code         = 404
    response_code      = 200
    response_page_path = "/index.html"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = { Name = "booktour-frontend-cdn" }
}

# Chi cho phep dung CloudFront distribution nay (khong phai bat ky CloudFront nao khac)
# doc object trong bucket.
resource "aws_s3_bucket_policy" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "AllowCloudFrontServicePrincipal"
      Effect    = "Allow"
      Principal = { Service = "cloudfront.amazonaws.com" }
      Action    = "s3:GetObject"
      Resource  = "${aws_s3_bucket.frontend.arn}/*"
      Condition = {
        StringEquals = {
          "AWS:SourceArn" = aws_cloudfront_distribution.frontend.arn
        }
      }
    }]
  })
}
