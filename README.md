# Booking Tour Project

Dự án Hệ thống Đặt Tour Du lịch (Booking Tour) là một ứng dụng Full-Stack hiện đại, được xây dựng với mục tiêu quản lý và cung cấp dịch vụ đặt tour trực tuyến.

## 🚀 Công nghệ sử dụng

### Frontend (Client-side)
* **Framework:** React 19 với Vite
* **Ngôn ngữ:** TypeScript
* **Quản lý State:** Zustand, TanStack React Query
* **Routing:** React Router DOM
* **Đa ngôn ngữ (i18n):** react-i18next
* **Giao diện & UI:** Recharts (biểu đồ), SweetAlert2, React Icons
* **Xử lý tài liệu:** html2canvas, jspdf, react-to-print (xuất PDF)
* **Xác thực:** Google OAuth (`@react-oauth/google`)
* **Gọi API:** Axios

### Backend (Server-side)
* **Framework:** Spring Boot 3.3.4 (Java 17)
* **Bảo mật:** Spring Security, JSON Web Token (JWT)
* **Database Access:** Spring Data JPA, Hibernate, Hypersistence Utils
* **Cơ sở dữ liệu:** PostgreSQL 16
* **Tích hợp thanh toán:** PayOS
* **Lưu trữ ảnh/media:** Cloudinary
* **Email:** Spring Boot Mail (Gửi email thông báo) với Thymeleaf (Template email)
* **Rate Limiting/Bảo vệ API:** Bucket4j

### Khác
* **Database Container:** Docker Compose
* **Môi trường:** dotenv (quản lý biến môi trường)

## 📁 Cấu trúc thư mục chính

```text
Booking_Tour/
├── backend/                # Source code backend (Spring Boot, Java 17)
├── frontend/               # Source code frontend (React, Vite, TS)
├── database/               # Các script hoặc dữ liệu liên quan DB
├── docker-compose.yml      # Cấu hình Docker để chạy PostgreSQL
└── README.md               # Tài liệu dự án
```

## 🛠️ Hướng dẫn cài đặt và chạy dự án

### 1. Khởi chạy Cơ sở dữ liệu (PostgreSQL)
Dự án sử dụng Docker Compose để khởi tạo nhanh CSDL PostgreSQL.
Tại thư mục gốc, chạy lệnh:
```bash
docker-compose up -d
```
*Lưu ý:* Cơ sở dữ liệu mặc định là `booktour_db`, user: `postgres`, mật khẩu: `quang`, port: `5435`.

### 2. Chạy Backend
Mở một terminal mới và di chuyển vào thư mục `backend`:
```bash
cd backend
```
Copy file `.env.example` thành `.env` (nếu cần) và điền các thông tin cần thiết (JWT, Database, Cloudinary, PayOS, Mail...).
Sau đó chạy project bằng Maven wrapper:
```bash
# Đối với Windows
mvnw.cmd spring-boot:run

# Đối với Linux/Mac
./mvnw spring-boot:run
```

### 3. Chạy Frontend
Mở một terminal mới và di chuyển vào thư mục `frontend`:
```bash
cd frontend
```
Cài đặt các gói phụ thuộc:
```bash
npm install
```
Khởi chạy môi trường phát triển:
```bash
npm run dev
```

## ✨ Các tính năng nổi bật
- **Đa ngôn ngữ (Bilingual):** Hỗ trợ chuyển đổi nhanh chóng giữa Tiếng Việt và Tiếng Anh trên toàn bộ giao diện.
- **Xác thực & Bảo mật:** Đăng nhập/Đăng ký với JWT và tích hợp đăng nhập nhanh qua Google OAuth.
- **Hệ thống tìm kiếm thông minh:** Tìm kiếm tour theo điểm đến, ngày đi, ngày về, và số lượng khách chính xác.
- **Quản lý chỗ ngồi thực tế (Real-time slots):** Tự động kiểm tra và giới hạn số lượng khách dựa trên số lượng chỗ (slots) còn trống của từng ngày khởi hành.
- **Giỏ hàng (Shopping Cart):** Hỗ trợ thêm nhiều tour vào giỏ hàng và thanh toán cùng lúc.
- **Thanh toán trực tuyến:** Tích hợp cổng thanh toán PayOS hiện đại, xác thực giao dịch tự động.
- **Email & PDF:** Gửi email thông báo tự động (Thymeleaf) và xuất vé điện tử E-Ticket dưới dạng PDF trực tiếp trên trình duyệt.
- **Trang Quản trị (Admin Dashboard):** Thống kê doanh thu qua biểu đồ trực quan, quản lý đầy đủ Tour, Booking, Voucher, Khách sạn, Dịch vụ tiện ích.
- **Upload Media:** Tải và quản lý hình ảnh dễ dàng qua Cloudinary.

## 🚀 Triển khai (Deployment)
- **Frontend & Backend** đã được thiết lập sẵn cấu hình để triển khai dễ dàng trên **Render** hoặc các nền tảng đám mây khác.
- Database PostgreSQL có thể sử dụng Managed Database của Render hoặc các nhà cung cấp bên thứ ba (Supabase, Neon...).
