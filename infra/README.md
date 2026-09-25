# Deploy Booking_Tour lên AWS

Hạ tầng: EC2 (backend + Postgres + Redis qua Docker Compose) + S3/CloudFront (frontend) +
CloudFront thứ 2 làm TLS trước EC2. Chi tiết kiến trúc xem plan gốc.

**Chưa gắn domain riêng** (chưa truy cập được DNS của `booktour.store`) — cả 2 CloudFront
dùng thẳng domain mặc định `*.cloudfront.net`, vẫn có HTTPS miễn phí tự động (chứng chỉ mặc
định của CloudFront). Đăng nhập Google vẫn hoạt động bình thường trên domain này, chỉ cần khai
báo đúng domain đó trong Google Console (xem mục cuối). Khi nào truy cập lại được DNS, có thể
thêm domain riêng + ACM sau mà không phải dựng lại từ đầu.

**`terraform apply`/`destroy` chạy trên GitLab CI** (không chạy cục bộ) — state lưu trên **S3**
(bucket riêng, tạo 1 lần qua job `bootstrap-state-bucket`), AWS credentials chỉ nằm trong
GitLab CI/CD Variables, không cần cài gì thêm trên máy bạn.

**Quản trị EC2 qua AWS Systems Manager (SSM) Session Manager** — không mở port SSH (22) ra
Internet, không cần SSH key, không phụ thuộc IP cá nhân. Xác thực qua IAM thay vì network.

## Chuẩn bị

Tại **GitLab → Settings → CI/CD → Variables**, thêm các biến sau:

| Key | Giá trị | Protected | Masked |
|---|---|---|---|
| `AWS_ACCESS_KEY_ID` | Access Key của IAM user | Có | Có |
| `AWS_SECRET_ACCESS_KEY` | Secret Key của IAM user | Có | Có |
| `TF_VAR_budget_email` | Email nhận cảnh báo AWS Budget | Có | Không |

"Protected" nghĩa là biến chỉ khả dụng trên các nhánh/tag được đánh dấu protected — đảm bảo
nhánh `main` (và `dev` nếu muốn test trước) của bạn đang được đánh dấu Protected tại
**Settings → Repository → Protected branches**, nếu không job sẽ không đọc được các biến này.

`gitlab_project_path`, `aws_region`, `instance_type` đã có giá trị mặc định đúng trong
`infra/variables.tf` — không cần khai báo trừ khi muốn đổi.

## Dựng hạ tầng

Vào GitLab → **CI/CD → Pipelines** → chạy pipeline trên nhánh `main` (hoặc `dev` để test trước):

1. Bấm chạy job **`bootstrap-state-bucket`** (chỉ cần 1 lần duy nhất, chạy lại các lần sau
   vẫn an toàn — tự bỏ qua nếu bucket đã tồn tại).
2. Bấm chạy job **`terraform-plan`** — xem log để review trước những gì sẽ được tạo.
3. Bấm chạy job **`terraform-apply`** — tạo toàn bộ hạ tầng (VPC, EC2, S3, CloudFront x2,
   IAM OIDC, IAM Role cho SSM, Budget).
4. Xem log job `terraform-apply` để lấy các output: `elastic_ip`, `frontend_cloudfront_domain`,
   `api_cloudfront_domain`, `frontend_bucket_name`, `gitlab_ci_role_arn`,
   `frontend_cloudfront_distribution_id`, `api_cloudfront_distribution_id`.

CloudFront có thể báo lỗi "Your account must be verified" nếu tài khoản AWS còn mới — đây là
giới hạn từ AWS, không phải lỗi cấu hình (xem AWS Support). EC2/backend vẫn tạo và chạy bình
thường, không bị ảnh hưởng — chạy lại `terraform-apply` sau khi tài khoản được xác minh để
tạo nốt phần CloudFront.

## Cập nhật các giá trị phụ thuộc vào output (bắt buộc, chỉ làm 1 lần sau apply đầu tiên)

Domain CloudFront chỉ biết được sau khi apply xong, nên cần điền lại 3 chỗ:

1. [frontend/.env.production](../frontend/.env.production): thay `REPLACE_WITH_API_CLOUDFRONT_DOMAIN`
   bằng giá trị output `api_cloudfront_domain`, commit lại.
2. `/opt/booking-tour/.env` trên EC2 (xem mục bên dưới): `FRONTEND_URL`,
   `PAYOS_RETURN_URL`, `PAYOS_CANCEL_URL` dùng `https://<frontend_cloudfront_domain>`.
3. Google Cloud Console: thêm `https://<frontend_cloudfront_domain>` vào Authorized
   JavaScript origins (xem mục Google OAuth bên dưới).

## Kết nối vào EC2 qua SSM Session Manager

Không dùng SSH. Vào **AWS Console → EC2 → Instances** → tick chọn instance `booktour-backend`
→ bấm **Connect** → tab **"In web browser"** → chọn **"SSM Session Manager"** → bấm **Connect**.
Mở ra 1 terminal ngay trong trình duyệt, đăng nhập sẵn (không cần key/mật khẩu).

Nếu muốn dùng từ terminal cục bộ thay vì trình duyệt, cài
[AWS CLI + Session Manager plugin](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html)
rồi chạy: `aws ssm start-session --target <instance-id> --region ap-southeast-1`.

## Cấu hình GitLab (phần còn lại, sau khi có output ở trên)

1. **CI/CD Variables**, thêm tiếp (không cần Protected/Masked vì không phải giá trị bí mật,
   chỉ là ID/ARN công khai trong tài khoản của bạn):
   - `AWS_ROLE_ARN` = output `gitlab_ci_role_arn`
   - `S3_BUCKET` = output `frontend_bucket_name`
   - `CLOUDFRONT_DISTRIBUTION_ID` = output `frontend_cloudfront_distribution_id`
2. **Runner**: Settings → CI/CD → Runners → "New project runner", tag `aws-ec2`, executor
   `shell`. Lấy registration token, mở session SSM vào EC2 (như mục trên) và chạy theo hướng
   dẫn cài đặt GitLab Runner cho Ubuntu mà trang đó hiển thị, dùng đúng token và tag ở trên.
3. **Deploy Token** (Settings → Repository → Deploy tokens), scope `read_registry`. Trong
   session SSM đang mở, chạy 1 lần:
   ```bash
   sudo docker login registry.gitlab.com -u <deploy-token-username> -p <deploy-token>
   ```

## Tạo file môi trường production trên EC2

Trong session SSM, tạo `/opt/booking-tour/.env` (không commit file này) với đầy đủ biến theo
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

Lưu ý: session SSM đăng nhập bằng user `ssm-user`, không phải `ubuntu` — dùng `sudo` khi cần
quyền ghi vào `/opt/booking-tour`.

## Google OAuth

Google Cloud Console → APIs & Services → Credentials → OAuth Client đang dùng → thêm
`https://<frontend_cloudfront_domain>` vào **Authorized JavaScript origins**.

## Chạy thử

Push lên nhánh `main` (hoặc mở PR từ `dev` rồi merge, đúng quy trình trong
[rule/CommitRule.md](../rule/CommitRule.md)) để 2 job `build-*`/`deploy-*` tự chạy — riêng các
job `terraform-*`/`bootstrap-state-bucket` luôn cần tự bấm Run thủ công, không bao giờ tự động
(tránh lỡ tay tạo/xóa hạ tầng thật).

## Nâng cấp lên domain riêng (khi truy cập lại được DNS của booktour.store)

Khi đó cần: tạo lại `aws_acm_certificate` (us-east-1, DNS validation) + thêm `aliases` vào 2
CloudFront distribution + đổi `viewer_certificate` sang dùng `acm_certificate_arn` thay vì
`cloudfront_default_certificate` + trỏ CNAME `www`/`api` tại PA. Đây là phần đã thiết kế sẵn
trong plan gốc, có thể làm lại bất kỳ lúc nào không ảnh hưởng phần đang chạy.

## Dọn dẹp (teardown)

Vào GitLab → CI/CD → Pipelines, chạy pipeline trên `main`, bấm **`terraform-destroy`** (job
thủ công ở stage `infra`) — xóa toàn bộ VPC/EC2/SG/EIP/S3/CloudFront/IAM (OIDC + SSM)/Budget.
Bucket chứa Terraform state (`booktour-tfstate-<project-id>`) không bị xóa (Terraform không tự
xóa backend của chính nó) — xóa thủ công qua S3 Console nếu muốn dọn sạch hoàn toàn.

Sau đó thủ công: xóa GitLab Runner registration (Settings → CI/CD → Runners), xóa Deploy
Token (Settings → Repository → Deploy tokens).
