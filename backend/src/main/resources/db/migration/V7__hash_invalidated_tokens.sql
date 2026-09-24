-- =============================================================================
-- V7: Luu SHA-256 cua token thay vi luu chinh chuoi JWT
-- =============================================================================
-- VAN DE GOC: entity InvalidatedToken khai @Column(length = 1024) nhung cot
-- that trong database la VARCHAR(255). Hibernate o che do validate KHONG kiem
-- tra do dai cot nen sai lech nay khong bao gio lo ra luc khoi dong.
--
-- Uoc luong do dai token that (HS256, subject = username):
--     header 20 + payload ~1.34 x (56 + do_dai_username) + chu ky 43 + 2 dau cham
-- Cot username la VARCHAR(100). Voi username 100 ky tu:
--     20 + 209 + 43 + 2 = 274 ky tu  >  255  -> INSERT that bai khi logout.
-- Voi username ngan (10-20 ky tu) chi khoang 140 nen chua ai gap.
--
-- GIAI PHAP: bam SHA-256 -> luon dung 64 ky tu hex. Tot hon la chi noi rong cot
-- len 1024:
--   1. Do dai co dinh, khong con phu thuoc vao do dai username hay so claim.
--   2. Index nho va nhanh hon nhieu - bang nay bi tra cuu o MOI request.
--   3. Lo ban sao database cung khong lay duoc token con hieu luc (ham mot chieu).
--
-- Di kem thay doi code: JwtService.hashToken(), AuthServiceImpl.logout() va
-- JwtAuthenticationFilter - CA HAI noi phai bam giong het nhau.
--
-- AN TOAN: bang dang rong (0 dong) tai thoi diem viet migration nen khong can
-- chuyen doi du lieu. Lenh DELETE duoi day chi de phong truong hop co token
-- goc con sot lai o moi truong khac - nhung token do khong the bam nguoc, va
-- hau qua duy nhat la vai phien da logout duoc coi la con hieu luc cho den khi
-- het han.
-- =============================================================================

DELETE FROM invalidated_tokens WHERE LENGTH(id) <> 64;

ALTER TABLE invalidated_tokens ALTER COLUMN id TYPE VARCHAR(64);

-- Scheduler quet theo expiry_time de don ban ghi het han
CREATE INDEX IF NOT EXISTS idx_invalidated_tokens_expiry
    ON invalidated_tokens (expiry_time);
