-- =============================================================================
-- V8: UNIQUE mot phan cho users - chi ap dung voi tai khoan chua bi xoa mem
-- =============================================================================
-- VAN DE: users co xoa mem (@SQLDelete + @SQLRestriction) VA rang buoc UNIQUE
-- tren toan bang cho email/username. Hai co che nay danh nhau:
--
--   1. Admin xoa user A (email a@x.com) -> chi la UPDATE is_deleted = true,
--      dong du lieu van con.
--   2. A dang ky lai bang chinh email cu.
--   3. Code kiem tra "email da ton tai chua?" -> KHONG thay, vi @SQLRestriction
--      da loc bo cac dong is_deleted = true.
--   4. Lenh INSERT chay -> database chan bang UNIQUE.
--   5. Nguoi dung nhan loi "duplicate key value violates unique constraint
--      uk6dotkott2kjsp8vw4d0m25fb7" - loi ky thuat tho, khong phai thong bao
--      nghiep vu.
--
-- GIAI PHAP: doi sang UNIQUE MOT PHAN. Rang buoc chi ap dung cho tai khoan con
-- hoat dong, nen email cua tai khoan da xoa duoc giai phong.
--
-- QUYET DINH NGHIEP VU: neu he thong MUON cam vinh vien viec dung lai email cua
-- tai khoan da xoa (vi du de chan lam dung khuyen mai cho nguoi dung moi) thi
-- KHONG nen chay migration nay - giu rang buoc toan bang moi la dung y do.
-- =============================================================================

-- Xoa cac rang buoc UNIQUE hien co tren bang users. Dung vong lap thay vi goi
-- thang ten vi ten do Hibernate sinh ngau nhien (uk6dotkott2kjsp8vw4d0m25fb7,
-- ukr43af9ap4edm43mmtq01oddj6) va co the khac nhau giua cac moi truong.
DO $$
DECLARE con_record record;
BEGIN
    FOR con_record IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE rel.relname = 'users'
          AND nsp.nspname = 'public'
          AND con.contype = 'u'
    LOOP
        EXECUTE format('ALTER TABLE users DROP CONSTRAINT %I', con_record.conname);
        RAISE NOTICE 'Da xoa rang buoc UNIQUE cu: %', con_record.conname;
    END LOOP;
END $$;

-- Tai khoan da xoa mem khong con giu cho email/username nua
CREATE UNIQUE INDEX uq_users_email_active
    ON users (email) WHERE is_deleted = false;

CREATE UNIQUE INDEX uq_users_username_active
    ON users (username) WHERE is_deleted = false;

-- Van can index thuong tren toan bang: dang nhap va tra cuu ho so van can tim
-- nhanh theo username/email ke ca khi dong do da bi xoa mem.
CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
