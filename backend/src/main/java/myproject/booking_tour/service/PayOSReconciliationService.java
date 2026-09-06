package myproject.booking_tour.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Ghi ket qua doi soat cua MOT giao dich PayOS, trong transaction cua rieng no.
 *
 * VI SAO PHAI LA MOT BEAN RIENG, KHONG PHAI MOT PHUONG THUC TRONG SCHEDULER:
 *
 * Truoc day toan bo vong lap doi soat nam trong MOT @Transactional duy nhat.
 * Khi mot payment gap su co - vi du chu tai khoan da xoa tai khoan nen
 * cancelBooking nem ResourceNotFoundException, hoac khoa lac quan cua
 * TourSchedule that bai - ngoai le do di qua ranh gioi transaction cua
 * BookingService va Spring danh dau transaction dung chung la ROLLBACK-ONLY.
 * Vong lap co bat ngoai le va chay tiep, nhung la co do khong go duoc: den luc
 * commit, MOI payment da xac nhan thanh cong trong ca luot quet deu bi huy sach,
 * kem UnexpectedRollbackException.
 *
 * Te hon nua, viec do tu lap lai: rollback huy luon ca viec danh dau payment
 * gay loi, nen no van con PENDING va lai duoc quet o lan sau, mai mai.
 *
 * Tach thanh bean rieng khien moi lan goi di qua proxy cua Spring va mo mot
 * transaction moi. Mot payment hong chi lam hong chinh no.
 *
 * LUU Y: bat buoc phai la BEAN KHAC. Goi this.applyStatus(...) tu trong cung
 * mot lop se di thang, khong qua proxy, va @Transactional mat tac dung hoan toan.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSReconciliationService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    /**
     * Dong bo trang thai cua mot payment theo dieu PayOS noi.
     *
     * @param paymentId   id trong bang payments
     * @param payOSStatus trang thai doc duoc tu PayOS (xem PayOSStatusReader)
     * @return true neu co thay doi duoc ghi xuong database
     */
    @Transactional
    public boolean applyStatus(Long paymentId, String payOSStatus) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            return false;
        }

        // Doc lai trang thai BEN TRONG transaction. Giua luc scheduler liet ke
        // danh sach va luc chay toi day, webhook hoac endpoint /verify co the da
        // xu ly xong payment nay - luc do khong con gi de lam.
        if (!"PENDING".equals(payment.getPaymentStatus())) {
            return false;
        }

        Booking booking = payment.getBooking();

        if ("PAID".equals(payOSStatus)) {
            if (!"PAID".equals(booking.getStatus())) {
                booking.setStatus("PAID");
                bookingRepository.save(booking);
            }
            payment.setPaymentStatus("SUCCESS");
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);
            log.info("[DoiSoat] Payment {} (orderCode {}) da thanh toan -> SUCCESS",
                    paymentId, payment.getOrderCode());
            return true;
        }

        if ("CANCELLED".equals(payOSStatus) || "EXPIRED".equals(payOSStatus)) {
            if (!"CANCELLED".equals(booking.getStatus())) {
                bookingService.cancelBooking(booking.getId(), booking.getUser().getId());
            }
            payment.setPaymentStatus("FAILED");
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);
            log.info("[DoiSoat] Payment {} (orderCode {}) da huy/het han -> FAILED",
                    paymentId, payment.getOrderCode());
            return true;
        }

        // Cac trang thai khac (PENDING, PROCESSING...): chua ket luan duoc, de yen.
        return false;
    }
}
