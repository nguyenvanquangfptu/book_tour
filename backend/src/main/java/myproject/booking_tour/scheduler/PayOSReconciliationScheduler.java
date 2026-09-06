package myproject.booking_tour.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.entity.Payment;
import myproject.booking_tour.repository.PaymentRepository;
import myproject.booking_tour.service.PayOSReconciliationService;
import myproject.booking_tour.service.PayOSStatusReader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Luoi an toan cuoi cung cho thanh toan PayOS.
 *
 * Webhook la duong chinh, endpoint /verify la duong phu khi nguoi dung quay lai
 * trang web. Ca hai deu co the khong den: webhook that lac khi server dang sap,
 * con nguoi dung thi co quyen dong tab ngay sau khi chuyen khoan. Cong viec dinh
 * ky nay quet lai moi payment con PENDING va hoi thang PayOS.
 *
 * KHONG CO @Transactional O DAY, VA DO LA CO Y.
 *
 * Ba dieu phu thuoc vao viec khong mo transaction tai day:
 *
 *   1. Cach ly loi. Moi payment duoc xu ly trong transaction rieng cua
 *      PayOSReconciliationService. Mot payment hong khong keo nhung payment da
 *      xac nhan thanh cong xuong theo - loi ma phien ban truoc mac phai.
 *   2. Khong giu transaction trong luc cho mang. Lenh hoi PayOS la request HTTP
 *      dong bo; 100 payment la 100 lan cho. Giu mot transaction mo suot thoi
 *      gian do se giam connection trong pool va chan autovacuum cua PostgreSQL.
 *   3. Loi hien ra duoc. Phien ban truoc nuot ngoai le bang System.err.println,
 *      con ngoai le that su (UnexpectedRollbackException) thi no ra SAU khi
 *      phuong thuc da ket thuc, voi thong diep khong nhac ten payment nao.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayOSReconciliationScheduler {

    private final PaymentRepository paymentRepository;
    private final PayOSStatusReader payOSStatusReader;
    private final PayOSReconciliationService reconciliationService;

    @Scheduled(fixedRate = 1800000) // 30 phut
    public void reconcilePendingPayments() {
        List<Payment> pending = paymentRepository.findByPaymentStatusAndOrderCodeNotNull("PENDING");
        if (pending.isEmpty()) {
            return;
        }

        log.info("[DoiSoat] Bat dau kiem tra {} giao dich PayOS dang cho...", pending.size());

        int changed = 0;
        int failed = 0;

        for (Payment payment : pending) {
            Long paymentId = payment.getId();
            String orderCode = payment.getOrderCode();
            try {
                // Hoi PayOS TRUOC, ngoai moi transaction.
                String status = payOSStatusReader.statusOf(orderCode);
                if (reconciliationService.applyStatus(paymentId, status)) {
                    changed++;
                }
            } catch (Exception e) {
                // Bat o day la an toan: transaction cua payment nay (neu co) da
                // dong lai truoc khi ngoai le den duoc cho nay, nen khong con la
                // co rollback nao dinh vao cac payment tiep theo.
                failed++;
                log.error("[DoiSoat] Bo qua payment {} (orderCode {}): {}",
                        paymentId, orderCode, e.getMessage(), e);
            }
        }

        log.info("[DoiSoat] Hoan tat: {} thay doi, {} loi, {} khong doi.",
                changed, failed, pending.size() - changed - failed);
    }
}
