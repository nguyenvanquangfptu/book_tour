-- =============================================================================
-- V6: Noi bookings voi tour_schedules bang khoa ngoai that
-- =============================================================================
-- VAN DE: booking tru cho cua mot ngay khoi hanh cu the, nhung lien ket chi la
-- NGAM - service tra cuu bang cap (tour_id, travel_date). Khong co khoa ngoai
-- nen neu admin xoa hoac doi ngay cua mot tour_schedule, database khong ngan
-- can, va cac booking tro toi ngay do tro thanh mo coi trong im lang.
--
-- Thoi diem tot nhat de vá: kiem tra truoc khi viet migration cho thay hien co
-- 0 booking mo coi, nen phan backfill duoi day khop 100%.
--
-- PHAM VI: mot booking thuc te chiem cho cua NHIEU dong tour_schedules (mot
-- dong moi ngay trong suot thoi gian tour). Cot schedule_id nay chi tro toi
-- NGAY KHOI HANH - dung y nghia voi cot travel_date da co. Muc tieu la co khoa
-- ngoai that cho ngay khoi hanh, khong phai mo hinh hoa day du N ngay; viec do
-- can them bang trung gian booking_schedules va khong nam trong migration nay.
--
-- Di kem thay doi code: entity Booking (truong schedule) va
-- BookingServiceImpl.validateAndDeductTourSchedule (tra ve lich ngay khoi hanh).
-- =============================================================================

ALTER TABLE bookings ADD COLUMN schedule_id BIGINT;

-- Backfill: noi theo dung cap ma service dang dung de tra cuu
UPDATE bookings b
   SET schedule_id = s.id
  FROM tour_schedules s
 WHERE s.tour_id = b.tour_id
   AND s.departure_date = b.travel_date;

ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_schedule FOREIGN KEY (schedule_id)
        REFERENCES tour_schedules (id);

CREATE INDEX idx_bookings_schedule ON bookings (schedule_id);

-- Cot de NULL duoc: booking cu khong tim thay lich tuong ung, va truong hop
-- travel_date la NULL. Khong dat NOT NULL de khong pha vo du lieu lich su.
