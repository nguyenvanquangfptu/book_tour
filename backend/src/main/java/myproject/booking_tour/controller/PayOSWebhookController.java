package myproject.booking_tour.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.exception.WebhookVerificationException;
import myproject.booking_tour.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment/payos_transfer_handler")
public class PayOSWebhookController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private vn.payos.PayOS payOS;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Webhook PayOS goi vao khi trang thai giao dich thay doi.
     *
     * Hop dong cua cong thanh toan: 2xx = "da nhan va xu ly xong, dung gui lai",
     * non-2xx = "gui lai di". Vi vay phai tach bach hai loai loi:
     *
     *  - Loi VINH VIEN (chu ky sai, payload rac, orderCode khong ton tai):
     *    tra 200 kem success=false. Gui lai 1000 lan cung the, retry chi lam
     *    day log va dot rate limit.
     *
     *  - Loi TAM THOI (DB mat ket noi, PayOS API timeout): tra 503 de PayOS
     *    retry. Neu tra 400 nhu truoc day, PayOS coi la loi phia client va co
     *    the bo luon -> user da tra tien nhung booking van ket o PENDING.
     *
     * Body tra ve luon toi gian: chi tiet loi chi ghi vao log, khong ro ri
     * stack trace / thong tin noi bo ra ben thu ba.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> handlePayOSWebhook(@RequestBody Object body) {
        Webhook webhookBody;
        try {
            webhookBody = objectMapper.convertValue(body, Webhook.class);
        } catch (IllegalArgumentException e) {
            log.warn("Webhook PayOS co payload khong doc duoc, bo qua (khong retry)", e);
            return ResponseEntity.ok(Map.of("success", "false"));
        }

        try {
            paymentService.processPayOSWebhook(webhookBody);
            return ResponseEntity.ok(Map.of("success", "true"));
        } catch (WebhookVerificationException e) {
            // Loi vinh vien -> tra 2xx de PayOS ngung retry
            log.warn("Webhook PayOS khong hop le, bo qua (khong retry): {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", "false"));
        } catch (Exception e) {
            // Loi tam thoi -> tra 5xx de PayOS gui lai
            log.error("Loi he thong khi xu ly webhook PayOS, yeu cau PayOS retry", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("success", "false"));
        }
    }

    private myproject.booking_tour.security.CustomUserDetails getCurrentUserDetails() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof myproject.booking_tour.security.CustomUserDetails) {
            return (myproject.booking_tour.security.CustomUserDetails) authentication.getPrincipal();
        }
        throw new myproject.booking_tour.exception.BadRequestException("User is not authenticated");
    }

    /**
     * Trang PaymentSuccess goi vao day de doi chieu lai trang thai don hang voi
     * PayOS.
     *
     * Truoc day ca than ham nam trong mot khoi try bat Exception roi nem lai
     * BadRequestException kem nguyen e.getMessage(). Hai cai gia phai tra:
     *
     *  - Moi su co noi bo - PayOS timeout, database mat ket noi - deu tra 400
     *    kem nguyen van thong bao loi cua tang duoi. Do la 500 doi lot 400, va
     *    la mot duong ro ri chi tiet he thong ra ngoai.
     *
     *  - Chinh BadRequestException cua chot kiem tra quyen ngay ben tren cung
     *    bi khoi catch do nuot roi boc lai, nen cau "Ban khong co quyen xac
     *    minh don hang nay!" bi doi thanh "Loi xac minh thanh toan: Ban khong
     *    co quyen...".
     *
     * Gio moi loai loi di duong cua no: sai quyen va orderCode khong phai so
     * deu la 400 kem ly do that, con su co goi PayOS thi ghi log ERROR kem
     * orderCode va tra 500 voi thong bao chung - dung nhu cach PaymentServiceImpl
     * doi xu voi mot cuoc goi PayOS that bai.
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestParam String orderCode) {
        myproject.booking_tour.security.CustomUserDetails userDetails = getCurrentUserDetails();
        boolean isAdmin = "ADMIN".equals(userDetails.getUser().getRole().getName());
        Long ownerId = paymentService.getPaymentOwnerUserIdByOrderCode(orderCode);
        // ownerId == null nghia la orderCode nay khong co trong database.
        // Truoc day dieu kien la "ownerId != null && ..." nen truong hop do
        // di THANG qua chot kiem tra quyen, va code van hoi PayOS - bien
        // endpoint thanh cong cu do trang thai don hang PayOS bat ky.
        if (!isAdmin && !userDetails.getUser().getId().equals(ownerId)) {
            throw new myproject.booking_tour.exception.BadRequestException("Bạn không có quyền xác minh đơn hàng này!");
        }

        long orderCodeLong;
        try {
            orderCodeLong = Long.parseLong(orderCode);
        } catch (NumberFormatException e) {
            throw new myproject.booking_tour.exception.BadRequestException("Mã đơn hàng không hợp lệ.");
        }

        try {
            vn.payos.model.v2.paymentRequests.PaymentLink paymentLink = payOS.paymentRequests().get(orderCodeLong);
            String payosStatus = paymentLink.getStatus() != null ? paymentLink.getStatus().name() : "UNKNOWN";

            if ("PAID".equals(payosStatus)) {
                // Call processPayOSCallback manually to share logic
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("orderCode", orderCode);
                params.put("status", "PAID");
                paymentService.processPayOSCallback(params);
                return ResponseEntity.ok(Map.of("success", "true", "status", "PAID"));
            }
            return ResponseEntity.ok(Map.of("success", "false", "status", payosStatus));
        } catch (Exception e) {
            log.error("Không xác minh được thanh toán cho orderCode={}", orderCode, e);
            throw new IllegalStateException("Không xác minh được trạng thái thanh toán.", e);
        }
    }
}
