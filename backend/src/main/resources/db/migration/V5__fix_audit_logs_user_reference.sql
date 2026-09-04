-- =============================================================================
-- V5: Sua tham chieu nguoi dung trong audit_logs
-- =============================================================================
-- VAN DE: TourServiceImpl ghi nhat ky voi user_id = 0 kem comment
--     "Assuming system action since we don't have current user context here"
-- Trong bang users KHONG co user nao id = 0. Vi vay:
--   - Khong dat duoc khoa ngoai (moi ban ghi log se vi pham ngay)
--   - Nhat ky ghi lai "ai sua gia tour" nhung khong biet ai that su lam
--     -> mat gan het gia tri cua mot audit log
--
-- Cot user_id von DA cho phep NULL. NULL moi la cach dung de dien dat "khong ro
-- nguoi thuc hien" (tac vu he thong), khong phai so 0.
--
-- Di kem thay doi code: SecurityUtil.getCurrentUserId() va 5 cho ghi log trong
-- TourServiceImpl.
--
-- LUU Y ve entity_id: cot nay KHONG the co khoa ngoai vi day la tham chieu da
-- hinh - du lieu that co ca entity_name='Tour' (tro toi tours.id) lan
-- entity_name='User' (tro toi users.id). SQL khong co cu phap cho khoa ngoai
-- tro toi nhieu bang tuy theo gia tri cot khac.
-- =============================================================================

UPDATE audit_logs SET user_id = NULL WHERE user_id = 0;

ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_audit_logs_user ON audit_logs (user_id);

-- Tra cuu nhat ky theo doi tuong: "tour nay da bi sua nhung gi"
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_name, entity_id);

-- created_at truoc day do code tu gan; dat default o database de ban ghi ghi
-- bang SQL tay cung co moc thoi gian.
ALTER TABLE audit_logs ALTER COLUMN created_at SET DEFAULT NOW();
