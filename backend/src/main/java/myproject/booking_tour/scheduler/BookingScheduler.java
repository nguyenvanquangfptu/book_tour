package myproject.booking_tour.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.service.BookingAutoCancelService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Huy tu dong cac don da duoc duyet nhung khach khong thanh toan dung han.
 *
 * TRUOC DAY CO HAI BAN CUA VIEC NAY, VA CA HAI DEU HONG THEO MOT KIEU RIENG:
 *
 *   - Ban trong lop nay quet findByStatus("APPROVED"). Khong mot dong code nao
 *     trong du an dat trang thai "APPROVED" (chi co "CONFIRMED"), nen no chay
 *     moi phut de tra ve danh sach rong - vinh vien. Te hon: no set thang
 *     status = CANCELLED ma khong hoan cho, khong hoan voucher. Neu ai do
 *     "sua" chuoi trang thai cho khop, so cho se ro ri im lang.
 *
 *   - Ban trong BookingServiceImpl.autoCancelUnpaidBookings chay that, nhung
 *     dat ca vong lap trong mot @Transactional (xem BookingAutoCancelService
 *     de biet vi sao dieu do nguy hiem).
 *
 * Ban hop nhat nay giu lai phan dung cua ca hai: lop scheduler mong, khong mo
 * transaction, giao tung don cho BookingAutoCancelService xu ly rieng biet.
 *
 * KHONG CO @Transactional O DAY, VA DO LA CO Y - giong het
 * PayOSReconciliationScheduler:
 *
 *   1. Cach ly loi. Mot don hong khong keo nhung don da huy thanh cong xuong
 *      theo.
 *   2. Khong giu transaction trong luc gui email (gui mail la I/O cham).
 *   3. Loi hien ra duoc, kem so hieu don, ngay tai vong lap.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    /** Khach co 24 gio ke tu luc don duoc duyet de hoan tat thanh toan. */
    private static final int PAYMENT_GRACE_HOURS = 24;

    private final BookingRepository bookingRepository;
    private final BookingAutoCancelService autoCancelService;

    @Scheduled(fixedRate = 3600000) // moi gio
    public void cancelUnpaidBookings() {
        List<Booking> confirmed = bookingRepository.findByStatus("CONFIRMED");
        if (confirmed.isEmpty()) {
            return;
        }

        LocalDateTime deadline = LocalDateTime.now().minusHours(PAYMENT_GRACE_HOURS);
        log.info("[HuyDonQuaHan] Bat dau kiem tra {} don da duyet...", confirmed.size());

        int cancelled = 0;
        int failed = 0;

        for (Booking booking : confirmed) {
            // Chi lay id o day. Moi thu khac (user, tour) deu la lien ket LAZY,
            // doc o ngoai transaction se no LazyInitializationException.
            Long bookingId = booking.getId();
            try {
                if (autoCancelService.cancelIfStillOverdue(bookingId, deadline)) {
                    cancelled++;
                    log.info("[HuyDonQuaHan] Da huy don #{} do qua han thanh toan.", bookingId);
                }
            } catch (Exception e) {
                // Bat o day an toan: transaction cua don nay da dong lai truoc
                // khi ngoai le den duoc cho nay, nen khong con co rollback nao
                // dinh vao cac don tiep theo.
                failed++;
                log.error("[HuyDonQuaHan] Bo qua don #{}: {}", bookingId, e.getMessage(), e);
            }
        }

        log.info("[HuyDonQuaHan] Hoan tat: {} don bi huy, {} loi, {} van con han.",
                cancelled, failed, confirmed.size() - cancelled - failed);
    }
}
