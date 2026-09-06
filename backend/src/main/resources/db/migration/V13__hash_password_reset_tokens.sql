-- =============================================================================
-- V13: Luu SHA-256 cua ma OTP thay vi luu ma goc
-- =============================================================================
-- VAN DE: cot "token" luu thang ma OTP 6 so duoi dang van ban thuong:
--
--     id | token  |         email
--      2 | 624508 | vanquangqn28@gmail.com
--      3 | 516859 | uitest01@example.com
--
-- Mot ma OTP con han la doi duoc mat khau cua tai khoan tuong ung, khong can
-- biet mat khau cu. Nen bang nay tuong duong mot danh sach chia khoa de tran.
-- Bat cu ai doc duoc database - ban sao luu bi lo, mot lo hong SQL injection,
-- hay chinh nguoi co quyen doc DB de van hanh - deu chiem duoc tai khoan.
--
-- KHONG NHAT QUAN VOI CHINH DU AN NAY: refresh_tokens da bam SHA-256 tu V12,
-- va invalidated_tokens cung da lam vay tu V7. Chi rieng OTP - thu nhay cam
-- nhat trong ba loai - lai de tran.
--
-- GIAI PHAP: bam SHA-256, luon dung 64 ky tu hex. Ban goc chi con ton tai
-- trong email gui di. Khong can salt: dau vao khong phai mat khau nguoi dung
-- tu dat, va ma chi song 5 phut.
--
-- DOI TEN COT "token" -> "token_hash" de ten goi khong noi doi ve noi dung,
-- giong hoan toan cach dat ten cua refresh_tokens.
--
-- XOA HET DU LIEU CU: ham bam mot chieu nen khong the chuyen doi ma dang co
-- sang dang bam ma van giu duoc y nghia. Hau qua duy nhat la nguoi dung nao
-- dang giu ma cu phai bam "gui lai ma" - ma OTP von chi song 5 phut nen gan
-- nhu chac chan chung da het han. V7 da xu ly invalidated_tokens y het cach nay.
--
-- Di kem thay doi code: entity PasswordResetToken, PasswordResetTokenRepository
-- .findByTokenHash(), va AuthServiceImpl - CA HAI noi (luc tao va luc doi mat
-- khau) phai bam giong het nhau, neu khong viec doi mat khau se im lang khong
-- bao gio tim thay ma nao.
-- =============================================================================

DELETE FROM password_reset_tokens;

ALTER TABLE password_reset_tokens RENAME COLUMN token TO token_hash;

ALTER TABLE password_reset_tokens ALTER COLUMN token_hash TYPE VARCHAR(64);
