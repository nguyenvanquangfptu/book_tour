-- =============================================================================
-- V16: UNIQUE mot phan cho ma voucher - chi ap dung voi voucher chua bi xoa mem
-- =============================================================================
-- VAN DE: y het V8 lam cho users. vouchers co xoa mem (@SQLDelete + @Where) va
-- rang buoc UNIQUE (code) tren TOAN BANG:
--
--   1. Admin xoa voucher "TET". Dong van nam do voi is_deleted = true.
--   2. Nam sau admin tao lai voucher ma "TET".
--   3. VoucherServiceImpl.createVoucher hoi existsByCode("TET") - cau truy van
--      di qua @Where nen KHONG thay dong da xoa, tra ve false.
--   4. INSERT chay -> database chan bang UNIQUE -> 409 "Du lieu vua duoc thay
--      doi boi mot giao dich khac. Vui long thu lai!" - thu lai bao nhieu lan
--      cung vay.
--
-- Trang quan tri khong co thung rac cho voucher, nen admin khong thay, khong
-- khoi phuc duoc voucher cu, va vinh vien khong dung lai duoc ma do.
--
-- GIAI PHAP: UNIQUE chi tren nhung voucher con song. Booking tro toi voucher
-- qua voucher_id chu khong qua ma, nen hai dong cung ma (mot da xoa) khong lam
-- lech lich su dat tour nao.
-- =============================================================================

-- Ten rang buoc do Hibernate sinh ngau nhien (uk30ftp2biebbvpik8e49wlmady) va
-- co the khac giua cac moi truong - dung vong lap giong V8.
DO $$
DECLARE con_record record;
BEGIN
    FOR con_record IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE rel.relname = 'vouchers'
          AND nsp.nspname = 'public'
          AND con.contype = 'u'
    LOOP
        EXECUTE format('ALTER TABLE vouchers DROP CONSTRAINT %I', con_record.conname);
        RAISE NOTICE 'Da xoa rang buoc UNIQUE cu: %', con_record.conname;
    END LOOP;
END $$;

CREATE UNIQUE INDEX uq_vouchers_code_active
    ON vouchers (code) WHERE is_deleted = false;
