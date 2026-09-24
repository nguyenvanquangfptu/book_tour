-- =============================================================================
-- V10: Chuyen cac cot thoi gian sang TIMESTAMPTZ (co mui gio)
-- =============================================================================
-- VAN DE: toan bo 13 cot thoi gian deu la "timestamp without time zone", trong
-- khi BookingTourApplication.main() goi
--     TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
-- Nghia la du lieu DANG duoc luu theo UTC, nhung database khong he ghi nhan
-- dieu do. Hau qua:
--   - Trien khai len server o mui gio khac, hoac doi dong setDefault kia, thi
--     du lieu cu va moi lech nhau ma khong co cach nao phan biet.
--   - Cong cu khac (BI, script bao cao, pgAdmin) doc truc tiep vao database
--     khong biet cac moc thoi gian dang o mui gio nao.
-- Voi TIMESTAMPTZ, PostgreSQL luu moc thoi gian tuyet doi va tu quy doi theo
-- mui gio cua phien lam viec - khong con cho nham lan.
--
-- KHONG PHAI DOI ENTITY. Da kiem chung: giu nguyen 13 truong LocalDateTime,
-- Hibernate o che do validate van chap nhan cot timestamptz, va gia tri doc qua
-- API trung khop tung mili giay voi gia tri trong database. Dinh dang JSON
-- cung khong doi (van la "2026-06-26T09:37:57.031403", khong co hau to mui gio)
-- nen frontend khong phai sua gi.
--
-- *** DIEU KIEN QUAN TRONG ***
-- Su dung LocalDateTime cung TIMESTAMPTZ chi dung khi JVM chay o UTC, vi luc do
-- mui gio phien PostgreSQL cung la UTC va hai chieu doc/ghi khop nhau.
-- => TUYET DOI KHONG XOA dong TimeZone.setDefault(UTC) trong
--    BookingTourApplication. Neu bo dong do, moi moc thoi gian se bi dich theo
--    mui gio may chu.
-- Muon go bo rang buoc ngam nay thi phai doi 13 truong LocalDateTime sang
-- Instant hoac OffsetDateTime - viec lon hon, keo theo DTO va mapper.
--
-- AT TIME ZONE 'UTC' trong lenh duoi dien giai gia tri cu "khong mui gio" LA
-- gio UTC - dung voi cach du lieu da duoc ghi tu truoc toi nay.
-- =============================================================================

ALTER TABLE audit_logs
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

ALTER TABLE bookings
    ALTER COLUMN booking_date TYPE TIMESTAMPTZ USING booking_date AT TIME ZONE 'UTC';
ALTER TABLE bookings
    ALTER COLUMN approved_at  TYPE TIMESTAMPTZ USING approved_at  AT TIME ZONE 'UTC';

ALTER TABLE contact_messages
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

ALTER TABLE invalidated_tokens
    ALTER COLUMN expiry_time TYPE TIMESTAMPTZ USING expiry_time AT TIME ZONE 'UTC';

ALTER TABLE password_reset_tokens
    ALTER COLUMN expiry_date TYPE TIMESTAMPTZ USING expiry_date AT TIME ZONE 'UTC';

ALTER TABLE payments
    ALTER COLUMN payment_date TYPE TIMESTAMPTZ USING payment_date AT TIME ZONE 'UTC';

ALTER TABLE reviews
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

ALTER TABLE users
    ALTER COLUMN created_at                   TYPE TIMESTAMPTZ USING created_at                   AT TIME ZONE 'UTC';
ALTER TABLE users
    ALTER COLUMN forgot_password_last_attempt TYPE TIMESTAMPTZ USING forgot_password_last_attempt AT TIME ZONE 'UTC';
ALTER TABLE users
    ALTER COLUMN forgot_password_ban_until    TYPE TIMESTAMPTZ USING forgot_password_ban_until    AT TIME ZONE 'UTC';

ALTER TABLE vouchers
    ALTER COLUMN valid_from  TYPE TIMESTAMPTZ USING valid_from  AT TIME ZONE 'UTC';
ALTER TABLE vouchers
    ALTER COLUMN valid_until TYPE TIMESTAMPTZ USING valid_until AT TIME ZONE 'UTC';

-- Default cua audit_logs.created_at dat o V5 la NOW(), voi TIMESTAMPTZ thi NOW()
-- tra ve moc co mui gio - dat lai cho ro rang.
ALTER TABLE audit_logs ALTER COLUMN created_at SET DEFAULT NOW();
