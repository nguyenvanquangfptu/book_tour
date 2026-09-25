# Deploy Booking_Tour lên AWS

Hạ tầng: EC2 (backend + Postgres + Redis qua Docker Compose) + S3/CloudFront (frontend) +
CloudFront thứ 2 làm TLS trước EC2. Chi tiết kiến trúc xem plan gốc.

**Chưa gắn domain riêng** (chưa truy cập được DNS của `booktour.store`) — cả 2 CloudFront
dùng thẳng domain mặc định `*.cloudfront.net`, vẫn có HTTPS miễn phí tự động (chứng chỉ mặc
định của CloudFront). Đăng nhập Google vẫn hoạt động bình thường trên domain này, chỉ cần khai
báo đúng domain đó trong Google Console (xem mục cuối). Khi nào truy cập lại được DNS, có thể
thêm domain riêng + ACM sau mà không phải dựng lại từ đầu.

**`terraform apply`/`destroy` chạy trên GitLab CI** (không chạy cục bộ) — state lưu trên
GitLab (GitLab-managed Terraform state), AWS credentials chỉ nằm trong GitLab CI/CD Variables,
không cần cài gì thêm trên máy bạn ngoài việc tạo 1 cặp SSH key.

## Chuẩn bị

1. Tạo riêng 1 cặp SSH key để SSH vào EC2 sau này (khác với key SSH GitLab đã tạo trước đó):
   ```bash
   ssh-keygen -t ed25519 -C "ec2-admin" -f ~/.ssh/id_ed25519_ec2
   ```
2. Tại **GitLab → Settings → CI/CD → Variables**, thêm các biến sau:

   | Key | Giá trị | Protected | Masked |
   |---|---|---|---|
   | `AWS_ACCESS_KEY_ID` | Access Key của IAM user | Có | Có |
   | `AWS_SECRET_ACCESS_KEY` | Secret Key của IAM user | Có | Có |
   | `TF_VAR_admin_ip` | IP cá nhân dạng `/32` (tra tại whatismyip.com) | Có | Không |
   | `TF_VAR_ssh_public_key` | Nội dung file `~/.ssh/id_ed25519_ec2.pub` | Có | Không |
   | `TF_VAR_budget_email` | Email nhận cảnh báo AWS Budget | Có | Không |

   "Protected" nghĩa là biến chỉ khả dụng trên các nhánh/tag được đánh dấu protected —
   đảm bảo nhánh `main` của bạn đang được đánh dấu Protected tại **Settings → Repository →
   Protected branches**, nếu không job Terraform sẽ không đọc được các biến này.

   `gitlab_project_path`, `aws_region`, `instance_type` đã có giá trị mặc định đúng trong
   `infra/variables.tf` — không cần khai báo trừ khi muốn đổi.

## Dựng hạ tầng

Vào GitLab → **CI/CD → Pipelines** → chạy pipeline trên nhánh `main`:

1. Bấm chạy job **`terraform-plan`** (job thủ công, ở stage `infra`) — xem log để review
   trước những gì sẽ được tạo.
2. Bấm chạy job **`terraform-apply`** — tạo toàn bộ hạ tầng (VPC, EC2, S3, CloudFront x2,
   IAM OIDC, Budget).
3. Xem log job `terraform-apply` để lấy các output: `elastic_ip`, `frontend_cloudfront_domain`,
   `api_cloudfront_domain`, `frontend_bucket_name`, `gitlab_ci_role_arn`,
   `frontend_cloudfront_distribution_id`, `api_cloudfront_distribution_id`.

## Cập nhật các giá trị phụ thuộc vào output (bắt buộc, chỉ làm 1 lần sau apply đầu tiên)

Domain CloudFront chỉ biết được sau khi apply xong, nên cần điền lại 3 chỗ:

1. [frontend/.env.production](../frontend/.env.production): thay `REPLACE_WITH_API_CLOUDFRONT_DOMAIN`
   bằng giá trị output `api_cloudfront_domain`, commit lại.
2. `/opt/booking-tour/.env` trên EC2 (xem mục bên dưới): `FRONTEND_URL`,
   `PAYOS_RETURN_URL`, `PAYOS_CANCEL_URL` dùng `https://<frontend_cloudfront_domain>`.
3. Google Cloud Console: thêm `https://<frontend_cloudfront_domain>` vào Authorized
   JavaScript origins (xem mục Google OAuth bên dưới).

## Cấu hình GitLab (phần còn lại, sau khi có output ở trên)

1. **CI/CD Variables**, thêm tiếp (không cần Protected/Masked vì không phải giá trị bí mật,
   chỉ là ID/ARN công khai trong tài khoản của bạn):
   - `AWS_ROLE_ARN` = output `gitlab_ci_role_arn`
   - `S3_BUCKET` = output `frontend_bucket_name`
   - `CLOUDFRONT_DISTRIBUTION_ID` = output `frontend_cloudfront_distribution_id`
2. **Runner**: Settings → CI/CD → Runners → "New project runner", tag `aws-ec2`, executor
   `shell`. Lấy registration token, SSH vào EC2 (`ssh -i ~/.ssh/id_ed25519_ec2 ubuntu@<elastic_ip>`)
   và chạy theo hướng dẫn cài đặt GitLab Runner cho Ubuntu mà trang đó hiển thị, dùng đúng
   token và tag ở trên.
3. **Deploy Token** (Settings → Repository → Deploy tokens), scope `read_registry`. SSH vào
   EC2, chạy 1 lần:
   ```bash
   docker login registry.gitlab.com -u <deploy-token-username> -p <deploy-token>
   ```

## Tạo file môi trường production trên EC2

SSH vào EC2, tạo `/opt/booking-tour/.env` (không commit file này) với đầy đủ biến theo
[backend/.env.example](../backend/.env.example) đã cập nhật, cộng thêm 3 biến chỉ dùng cho
Postgres container (không có trong `.env.example` vì đó là biến riêng của app):

```
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<mật khẩu bạn tự đặt>
POSTGRES_DB=booktour_db
```

Giá trị production cho các biến URL (thay `<frontend_cloudfront_domain>` bằng output thật):
```
FRONTEND_URL=https://<frontend_cloudfront_domain>
PAYOS_RETURN_URL=https://<frontend_cloudfront_domain>/payment/success
PAYOS_CANCEL_URL=https://<frontend_cloudfront_domain>/payment/cancel
```

## Google OAuth

Google Cloud Console → APIs & Services → Credentials → OAuth Client đang dùng → thêm
`https://<frontend_cloudfront_domain>` vào **Authorized JavaScript origins**.

## Chạy thử

Push lên nhánh `main` (hoặc mở PR từ `dev` rồi merge, đúng quy trình trong
[rule/CommitRule.md](../rule/CommitRule.md)) để 2 job `build-*`/`deploy-*` tự chạy — riêng các
job `terraform-*` luôn cần tự bấm Run thủ công, không bao giờ tự động (tránh lỡ tay tạo/xóa
hạ tầng thật).

## Nâng cấp lên domain riêng (khi truy cập lại được DNS của booktour.store)

Khi đó cần: tạo lại `aws_acm_certificate` (us-east-1, DNS validation) + thêm `aliases` vào 2
CloudFront distribution + đổi `viewer_certificate` sang dùng `acm_certificate_arn` thay vì
`cloudfront_default_certificate` + trỏ CNAME `www`/`api` tại PA. Đây là phần đã thiết kế sẵn
trong plan gốc, có thể làm lại bất kỳ lúc nào không ảnh hưởng phần đang chạy.

## Dọn dẹp (teardown)

Vào GitLab → CI/CD → Pipelines, chạy pipeline trên `main`, bấm **`terraform-destroy`** (job
thủ công ở stage `infra`) — xóa toàn bộ VPC/EC2/SG/EIP/S3/CloudFront/IAM OIDC/Budget.

Sau đó thủ công: xóa GitLab Runner registration (Settings → CI/CD → Runners), xóa Deploy
Token (Settings → Repository → Deploy tokens).
