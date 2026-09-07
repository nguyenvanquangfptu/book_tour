package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.BookingRequest;
import myproject.booking_tour.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, Long userId);
    List<BookingResponse> getMyBookings(Long userId);
    myproject.booking_tour.dto.response.PageResponse<BookingResponse> getAllBookings(int page, int size);
    BookingResponse getBookingById(Long id);
    BookingResponse confirmBooking(Long id);
    /**
     * Huy don do NGUOI DUNG khoi xuong. Kiem tra quyen so huu va cac quy dinh
     * danh cho khach (khong tu huy don da thanh toan, khong huy tour da khoi
     * hanh) - admin duoc mien nhung quy dinh do.
     */
    BookingResponse cancelBooking(Long id, Long userId);

    /**
     * Huy don do HE THONG khoi xuong: cong viec dinh ky huy don qua han, hoac
     * PayOS bao giao dich da huy/het han.
     *
     * Khac cancelBooking o hai diem, va ca hai deu co ly do:
     *
     *   - Khong kiem tra quyen so huu. Truoc day cac duong nay goi
     *     cancelBooking(id, booking.getUser().getId()) - truyen chinh chu don
     *     vao chot kiem tra quyen de no tu thoa man. Chot do khong bao ve duoc
     *     gi, chi lam viec huy that bai khi tai khoan chu don da bi xoa.
     *   - Khong nem ngoai le khi khong co gi de huy. He thong quet lai nhieu
     *     lan la chuyen binh thuong.
     *
     * Don da thanh toan KHONG bao gio bi huy o day: chuyen hoan tien phai do
     * nguoi that quyet dinh.
     *
     * @return true neu don vua bi huy trong lan goi nay
     */
    boolean cancelBookingBySystem(Long id, String reason);
}
