-- =============================================================================
-- V2: Them index cho cac cot khoa ngoai
-- =============================================================================
-- PostgreSQL KHONG tu tao index cho cot khoa ngoai (khac MySQL/InnoDB).
-- Truoc migration nay ca database chi co 3 index, deu la GIN tren cot JSONB,
-- 16 cot khoa ngoai deu khong co index nao.
--
-- Hau qua khi thieu:
--   1. Moi truy van loc theo khoa ngoai ("booking cua toi", "review cua tour
--      nay") deu la seq scan - quet toan bang.
--   2. Khi xoa mot dong cha, PostgreSQL phai quet bang con de kiem tra rang
--      buoc khoa ngoai -> thao tac xoa cung cham theo.
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_bookings_user       ON bookings (user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_tour       ON bookings (tour_id);
CREATE INDEX IF NOT EXISTS idx_bookings_voucher    ON bookings (voucher_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status     ON bookings (status);

CREATE INDEX IF NOT EXISTS idx_payments_booking    ON payments (booking_id);

CREATE INDEX IF NOT EXISTS idx_reviews_tour        ON reviews (tour_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user        ON reviews (user_id);

CREATE INDEX IF NOT EXISTS idx_cart_items_cart     ON cart_items (cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_tour     ON cart_items (tour_id);
CREATE INDEX IF NOT EXISTS idx_carts_user          ON carts (user_id);

CREATE INDEX IF NOT EXISTS idx_users_role          ON users (role_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_user ON password_reset_tokens (user_id);

-- Chieu nguoc lai cua hai bang trung gian N-N. Chieu tour_id da nam trong khoa
-- chinh ghep (tour_id, ...) nen khong can them.
CREATE INDEX IF NOT EXISTS idx_tour_acc_acc        ON tour_accommodations (accommodation_id);
CREATE INDEX IF NOT EXISTS idx_tour_util_util      ON tour_utilities (utility_id);

-- Index ghep phuc vu truy van nong nhat he thong: BookingServiceImpl tra cuu
-- so cho trong theo cap (tour_id, departure_date) moi lan co nguoi dat tour.
CREATE INDEX IF NOT EXISTS idx_tour_schedules_tour_date
    ON tour_schedules (tour_id, departure_date);

-- Index MOT PHAN: chi danh index cho tour chua bi xoa mem. Nho hon va nhanh hon
-- index day du vi bo qua toan bo cac dong is_deleted = true (hien la 44/52 dong).
CREATE INDEX IF NOT EXISTS idx_tours_active
    ON tours (status) WHERE is_deleted = false;
