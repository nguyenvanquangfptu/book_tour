package myproject.booking_tour.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Huy MOT don dat tour qua han thanh toan, trong transaction cua rieng no.
 *
 * VI SAO PHAI LA MOT BEAN RIENG, KHONG PHAI MOT PHUONG THUC TRONG SCHEDULER:
 *
 * Y het ly do da duoc ghi trong PayOSReconciliationService. Ban cu cua viec nay
 * (BookingServiceImpl.autoCancelUnpaidBookings) dat ca vong lap trong MOT
 * @Transactional roi goi cancelBooking cua chinh no. Mot don gap su co - chu
 * tai khoan da bi xoa, hoac lich khoi hanh khong con - lam Spring danh dau
 * transaction dung chung la ROLLBACK-ONLY, va moi don da huy thanh cong trong
 * luot quet do bi huy sach khi commit.
 *
 * Tach ra bean rieng khien moi lan goi di qua proxy cua Spring va mo mot
 * transaction moi. Mot don hong chi lam hong chinh no.
 *
 * LUU Y: bat buoc phai la BEAN KHAC. Goi this.cancelIfStillOverdue(...) tu
 * trong cung mot lop se di thang, khong qua proxy, va @Transactional mat tac
 * dung hoan toan.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingAutoCancelService {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final EmailService emailService;

    /**
     * Huy don neu no VAN con qua han tai thoi diem chay.
     *
     * @param bookingId id don can kiem tra
     * @param deadline  don duoc duyet truoc moc nay thi coi la qua han
     * @return true neu don vua bi huy
     */
    @Transactional
    public boolean cancelIfStillOverdue(Long bookingId, LocalDateTime deadline) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return false;
        }

        // Doc lai trang thai BEN TRONG transaction. Giua luc scheduler liet ke
        // danh sach va luc chay toi day, webhook PayOS hoac endpoint /verify co
        // the da chuyen don sang PAID - ban cu khong kiem tra lai va se huy
        // thang mot don vua duoc tra tien xong.
        if (!"CONFIRMED".equals(booking.getStatus())) {
            return false;
        }

        LocalDateTime approvedTime = booking.getApprovedAt() != null
                ? booking.getApprovedAt()
                : booking.getBookingDate();
        if (approvedTime == null || !approvedTime.isBefore(deadline)) {
            return false;
        }

        // cancelBookingBySystem la @Transactional tren mot bean khac,
        // propagation REQUIRED - no nhap vao dung transaction nay, khong mo
        // them cai moi.
        //
        // Duong he thong khong truyen id chu don vao chot kiem tra quyen de tu
        // thoa man no nua: chot do khong bao ve duoc gi, chi lam viec huy that
        // bai khi tai khoan chu don da bi xoa.
        if (!bookingService.cancelBookingBySystem(bookingId, "quá hạn thanh toán")) {
            return false;
        }

        sendAutoCancelEmail(booking);
        return true;
    }

    private void sendAutoCancelEmail(Booking booking) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("customerName", booking.getCustomerName() != null
                    ? booking.getCustomerName()
                    : booking.getUser().getFullName());
            templateModel.put("bookingId", "#" + booking.getId());
            templateModel.put("tourName", booking.getTour() != null ? booking.getTour().getTitle() : "Tour");

            String emailTo = booking.getCustomerEmail() != null
                    ? booking.getCustomerEmail()
                    : booking.getUser().getEmail();
            emailService.sendMessageUsingThymeleafTemplate(emailTo,
                    "Thông báo: Đơn đặt tour của bạn đã bị hủy tự động",
                    "booking-cancelled-auto", templateModel);
        } catch (Exception e) {
            // Gui mail hong khong duoc lam hong viec huy don - don da huy roi,
            // va mail thi khong co cach nao thu lai o day.
            log.error("[HuyDonQuaHan] Không gửi được email hủy đơn #{}: {}", booking.getId(), e.getMessage());
        }
    }
}
