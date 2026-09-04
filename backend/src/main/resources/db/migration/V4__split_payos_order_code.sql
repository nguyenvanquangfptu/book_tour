-- =============================================================================
-- V4: Tach ma don hang PayOS ra cot rieng
-- =============================================================================
-- VAN DE: cot payment_method dang chua HAI loai thong tin khac nhau.
-- Du lieu that truoc migration:
--     PAYOS_BANK_TRANSFER      <- PHUONG THUC thanh toan
--     PAYOS_17824664510005     <- MA DON HANG cua PayOS
--     PAYOS_17824007070003     <- MA DON HANG
-- Service tra cuu don hang bang findByPaymentMethod("PAYOS_" + orderCode).
--
-- BA HAU QUA:
--   1. Khong dat duoc UNIQUE tren ma don hang -> webhook goi lai hai lan dong
--      thoi van co the xu ly trung (van de idempotency).
--   2. Khong index duoc -> tra cuu la seq scan, ma webhook goi rat thuong xuyen.
--   3. Mot cot mang hai y nghia -> khong thong ke duoc "bao nhieu don tra bang
--      chuyen khoan" vi gia tri bi tron lan.
--
-- Migration nay PHAI deploy cung luc voi thay doi code (Payment,
-- PaymentRepository, PaymentServiceImpl).
-- =============================================================================

ALTER TABLE payments ADD COLUMN order_code VARCHAR(50);

-- Chuyen du lieu cu: chi lay cac dong ma phan sau "PAYOS_" la chuoi so thuan.
-- Dong "PAYOS_BANK_TRANSFER" khong khop nen order_code cua no van la NULL -
-- dung, vi do la phuong thuc chu khong phai ma don hang.
UPDATE payments
   SET order_code = SUBSTRING(payment_method FROM 7)
 WHERE payment_method LIKE 'PAYOS\_%'
   AND SUBSTRING(payment_method FROM 7) ~ '^[0-9]+$';

-- Tra payment_method ve dung y nghia cua no
UPDATE payments SET payment_method = 'PAYOS' WHERE payment_method LIKE 'PAYOS\_%';

-- UNIQUE o day chinh la manh ghep con thieu de webhook that su idempotent:
-- hai webhook den dong thoi khong the tao ra hai ban ghi cho cung mot don hang.
ALTER TABLE payments ADD CONSTRAINT uq_payments_order_code UNIQUE (order_code);

CREATE INDEX idx_payments_order_code ON payments (order_code);
