-- =============================================================================
-- V15: Gan ma OTP dat lai mat khau vao tai khoan, va dem so lan nhap sai
-- =============================================================================
-- VAN DE: POST /api/auth/reset-password chi nhan { token, newPassword }. Server
-- tra ma theo SHA-256 tren TOAN BANG, nen mot ma 6 so dung la doi duoc mat khau
-- cua BAT KY tai khoan nao dang giu ma do - ke goi khong can biet la tai khoan
-- nao. Va khong co gi dem so lan doan sai.
--
--   - Khong gian ma chi co 1.000.000 gia tri. Moi lan doan kiem tra cung luc
--     moi ma dang con han cua moi nguoi dung.
--   - Gioi han duy nhat la 10 request/phut moi IP o RateLimitingFilter. Du IP
--     thi du luot doan.
--
-- GIAI PHAP (di kem AuthServiceImpl.resetPassword):
--   1. Request phai kem email; server chi so voi ma cua DUNG tai khoan do.
--   2. Moi ma chi chiu toi da 5 lan nhap sai, sau do bi xoa va phai xin ma moi
--      (xin ma da co cooldown 1 phut va khoa 5 gio sau 3 lan).
--
-- Cot dem so lan sai nam o day chu khong o bang users vi no song cung ma: ma
-- moi thi dem lai tu dau.
-- =============================================================================

ALTER TABLE password_reset_tokens
    ADD COLUMN failed_attempts INTEGER NOT NULL DEFAULT 0;

-- UNIQUE tren token_hash (tu thoi cot con ten "token") chi co nghia khi ma la
-- khoa tra cuu toan bang. Gio moi ma duoc tra theo tai khoan, nen hai nguoi
-- tinh co boc trung mot ma 6 so la chuyen binh thuong - rang buoc cu lai bien no
-- thanh loi 409 luc xin ma. Ten rang buoc do Hibernate sinh ngau nhien nen
-- dung vong lap, giong V8.
DO $$
DECLARE con_record record;
BEGIN
    FOR con_record IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE rel.relname = 'password_reset_tokens'
          AND nsp.nspname = 'public'
          AND con.contype = 'u'
    LOOP
        EXECUTE format('ALTER TABLE password_reset_tokens DROP CONSTRAINT %I', con_record.conname);
        RAISE NOTICE 'Da xoa rang buoc UNIQUE cu: %', con_record.conname;
    END LOOP;
END $$;
