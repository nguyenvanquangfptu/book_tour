-- =============================================================================
-- V3: Rang buoc toan ven du lieu (UNIQUE + CHECK)
-- =============================================================================
-- Cac quy tac duoi day truoc nay chi duoc bao ve BANG CODE. Neu hai request
-- chay dong thoi, hoac ai do sua truc tiep bang SQL, code khong con la lop bao
-- ve. Database moi la noi cuoi cung dam bao tinh dung dan.
--
-- Da doi chieu voi du lieu that truoc khi viet: 0 gio hang trung, 0 review
-- trung, 0 lich khoi hanh trung, 0 rating ngoai khoang -> ap duoc ngay khong
-- can don du lieu.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. RANG BUOC DUY NHAT
-- -----------------------------------------------------------------------------

-- Entity Cart khai bao @OneToOne(unique = true) nhung schema khong co rang buoc
-- nay -> truoc migration nay mot user co the co nhieu gio hang.
ALTER TABLE carts
    ADD CONSTRAINT uq_carts_user UNIQUE (user_id);

-- Chan mot nguoi danh gia cung mot tour nhieu lan (lam lech tours.rating).
ALTER TABLE reviews
    ADD CONSTRAINT uq_reviews_user_tour UNIQUE (user_id, tour_id);

-- Chan hai dong lich cho cung mot tour trong cung mot ngay khoi hanh. Neu bi
-- trung, so cho se bi chia ra hai dong va viec tru cho tro nen sai.
ALTER TABLE tour_schedules
    ADD CONSTRAINT uq_tour_schedules_tour_date UNIQUE (tour_id, departure_date);

-- -----------------------------------------------------------------------------
-- 2. RANG BUOC GIA TRI SO
-- -----------------------------------------------------------------------------

ALTER TABLE reviews
    ADD CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5);

ALTER TABLE tour_schedules
    ADD CONSTRAINT ck_tour_schedules_slots CHECK (available_slots >= 0);

ALTER TABLE bookings
    ADD CONSTRAINT ck_bookings_people CHECK (number_of_people > 0);

ALTER TABLE bookings
    ADD CONSTRAINT ck_bookings_total_price CHECK (total_price >= 0);

ALTER TABLE tours
    ADD CONSTRAINT ck_tours_price CHECK (price >= 0);

ALTER TABLE vouchers
    ADD CONSTRAINT ck_vouchers_used CHECK (used_count >= 0);

ALTER TABLE vouchers
    ADD CONSTRAINT ck_vouchers_period CHECK (valid_until > valid_from);

ALTER TABLE vouchers
    ADD CONSTRAINT ck_vouchers_percentage CHECK (
        discount_percentage IS NULL OR discount_percentage BETWEEN 0 AND 100);

-- Voucher phai giam theo SO TIEN hoac theo PHAN TRAM, dung mot trong hai.
--
-- QUY UOC: du an dung so 0 (khong phai NULL) de danh dau "khong dung loai giam
-- gia nay". Du lieu that:
--     id 1: discount_amount = 0.00,    discount_percentage = 10  -> giam %
--     id 2: discount_amount = 1000.00, discount_percentage = 0   -> giam tien
-- Code cung theo quy uoc do (BookingServiceImpl kiem tra "compareTo(ZERO) > 0"
-- chu khong kiem tra NULL), nen rang buoc phai coi ca NULL lan 0 la "khong
-- dung" - neu khong se chan chinh du lieu hop le.
--
-- Rang buoc dong thoi chan hai trang thai vo nghia:
--   - ca hai deu 0   -> voucher khong giam gi
--   - ca hai deu > 0 -> nhap nhang; code hien uu tien "so tien" mot cach am tham
ALTER TABLE vouchers
    ADD CONSTRAINT ck_vouchers_discount_type CHECK (
        (COALESCE(discount_amount, 0) >  0 AND COALESCE(discount_percentage, 0) = 0)
     OR (COALESCE(discount_amount, 0) =  0 AND COALESCE(discount_percentage, 0) > 0)
    );

-- -----------------------------------------------------------------------------
-- 3. RANG BUOC TRANG THAI
-- -----------------------------------------------------------------------------
-- Danh sach gia tri duoi day duoc suy ra tu DU LIEU THAT + tim kiem toan bo
-- codebase (ca backend lan frontend), khong phai doan theo ten.
--
-- CANH BAO: moi lan them trang thai moi, PHAI sua rang buoc nay TRUOC, neu
-- khong lenh INSERT/UPDATE se that bai.

-- PENDING / PAID / CONFIRMED / CANCELLED deu duoc ghi trong code.
-- COMPLETED hien chi duoc DOC (ReviewServiceImpl kiem tra quyen danh gia) chu
-- chua bao gio duoc GHI - van giu trong danh sach de khong chan neu dung sau nay.
ALTER TABLE bookings
    ADD CONSTRAINT ck_bookings_status CHECK (
        status IS NULL OR status IN
        ('PENDING','PAID','CONFIRMED','CANCELLED','COMPLETED'));

ALTER TABLE payments
    ADD CONSTRAINT ck_payments_status CHECK (
        payment_status IS NULL OR payment_status IN
        ('PENDING','SUCCESS','FAILED'));

-- SOLD_OUT la trang thai thu ba, dung that o BookingServiceImpl va o giao dien
-- quan tri; du lieu hien co 2 tour dang o trang thai nay.
ALTER TABLE tours
    ADD CONSTRAINT ck_tours_status CHECK (
        status IS NULL OR status IN ('ACTIVE','INACTIVE','SOLD_OUT'));
