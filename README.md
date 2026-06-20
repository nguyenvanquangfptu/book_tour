# Booking Tour Project

Dự án Hệ thống Đặt Tour Du lịch (Booking Tour) là một ứng dụng Full-Stack hiện đại, được xây dựng với mục tiêu quản lý và cung cấp dịch vụ đặt tour trực tuyến.

## 🚀 Công nghệ sử dụng

### Frontend (Client-side)
* **Framework:** React 19 với Vite
* **Ngôn ngữ:** TypeScript
* **Quản lý State:** Zustand, TanStack React Query
* **Routing:** React Router DOM
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

## ✨ Các tính năng chính dự kiến
- Xác thực và phân quyền người dùng (JWT, Google OAuth).
- Quản lý và hiển thị danh sách các tour du lịch.
- Chức năng đặt tour trực tuyến.
- Tích hợp cổng thanh toán PayOS.
- Gửi email xác nhận tự động.
- Quản trị viên (Admin) quản lý tour, thống kê doanh thu với biểu đồ.
- Tải lên hình ảnh tour (thông qua Cloudinary).
- Xuất hóa đơn/chứng từ đặt tour dưới dạng PDF.
