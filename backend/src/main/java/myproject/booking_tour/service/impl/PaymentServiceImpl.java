package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.PaymentResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.PaymentMapper;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.PaymentRepository;
import myproject.booking_tour.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final myproject.booking_tour.service.PayOSReconciliationService reconciliationService;
    private final vn.payos.PayOS payOS;

    /**
     * Ghi nhan mot khoan thanh toan thu cong (vi du khach tra tien mat tai van
     * phong). CHI ADMIN duoc goi - xem SecurityConfig.
     *
     * Truoc day endpoint POST /api/payments nam chung trong /api/payments/**
     * (chi doi mot tai khoan bat ky) va phuong thuc nay khong he doc toi danh
     * tinh nguoi goi: bat cu khach hang nao cung tao duoc ban ghi payment tren
     * don cua nguoi khac, va doc duoc so tien phai tra cua ho qua truong amount
     * trong response.
     */
    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Không thể ghi nhận thanh toán cho đơn đã hủy.");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus("PENDING");
        payment.setPaymentDate(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id, Long userId, boolean isAdmin) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment invoice not found with id: " + id));
        if (!isAdmin && !payment.getBooking().getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền xem hóa đơn thanh toán này!");
        }
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, String status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment invoice not found with id: " + id));

        payment.setPaymentStatus(status);
        Payment updated = paymentRepository.save(payment);
        return paymentMapper.toResponse(updated);
    }

    /**
     * Truoc day day la findAll() roi loc trong Java: mot khach xem hoa don cua
     * minh keo CA BANG payments vao memory de tra ve vai dong. Loc dung, nhung
     * chi dung cho toi khi bang lon len.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments(Long userId, boolean isAdmin) {
        List<Payment> payments = isAdmin
                ? paymentRepository.findAll()
                : paymentRepository.findByBookingUserId(userId);
        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPaymentOwnerUserIdByOrderCode(String orderCode) {
        return paymentRepository.findByOrderCode(orderCode)
                .map(p -> p.getBooking().getUser().getId())
                .orElse(null);
    }

    // getPaymentsByBookingId da duoc go bo: khong controller nao goi toi no, va
    // ban than no khong co chot kiem tra quyen so huu - de nguyen thi chi cho
    // ai do noi no vao mot endpoint moi la thanh lo hong.


    @org.springframework.beans.factory.annotation.Value("${payos.return-url}")
    private String returnUrl;

    @org.springframework.beans.factory.annotation.Value("${payos.cancel-url}")
    private String cancelUrl;

    private String describeStatus(String status) {
        if ("PENDING".equals(status)) return "chưa được duyệt";
        if ("PAID".equals(status)) return "đã thanh toán";
        if ("CANCELLED".equals(status)) return "đã hủy";
        return "ở trạng thái hiện tại";
    }

    /**
     * Tao link thanh toan PayOS cho MOT booking.
     *
     * Truoc day phuong thuc nay khong he doc toi danh tinh nguoi goi: bat cu ai
     * co mot tai khoan CUSTOMER deu goi duoc voi bookingId cua nguoi khac, doc
     * ra so tien phai tra cua ho, va rai ban ghi payment rac vao don hang cua
     * ho. Day dung la lo hong da khien GET /api/payments/payos-callback bi go
     * bo (xem PaymentController) - chi khac la no van con o day.
     *
     * Trang thai cung phai kiem tra: chi don DA DUOC DUYET moi cho thanh toan.
     * Khong co chot nay thi tao duoc link cho don da huy hoac da tra tien roi,
     * va tien vao thi khong biet ghi vao dau.
     */
    @Override
    @Transactional
    public String createPaymentUrl(Long bookingId, Long userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!isAdmin && !booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền thanh toán cho đơn đặt tour này!");
        }

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new BadRequestException("Không thể tạo link thanh toán cho đơn "
                    + describeStatus(booking.getStatus()) + ".");
        }

        // Link PayOS khong duoc song lau hon han thanh toan cua don. Truoc day
        // link tao ra khong co expiredAt nen mo vo thoi han, trong khi
        // BookingScheduler huy don sau 24 gio va tra lai so cho. Khach mo lai
        // link cu va chuyen khoan van thanh cong - tien vao mot don da huy, cho
        // co the da ban cho nguoi khac.
        //
        // Scheduler chay moi gio, nen mot don co the con CONFIRMED them toi mot
        // gio sau khi qua han; khong tao link cho don o khoang do.
        LocalDateTime deadline = myproject.booking_tour.service.BookingAutoCancelService.paymentDeadlineOf(booking);
        if (deadline != null && !deadline.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Đơn đặt tour đã quá hạn thanh toán và sẽ bị hủy tự động.");
        }

        try {
            // Create a pending Payment record to track the transaction and use its ID as orderCode
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalPrice());
            payment.setPaymentMethod("PAYOS");
            payment.setPaymentStatus("PENDING");
            payment.setPaymentDate(LocalDateTime.now());
            Payment savedPayment = paymentRepository.save(payment);

            // Use a unique orderCode combining timestamp and payment ID to avoid PayOS duplicates
            long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis() / 1000) + String.format("%04d", savedPayment.getId() % 10000));
            savedPayment.setOrderCode(String.valueOf(orderCode));
            paymentRepository.save(savedPayment);

            // Giá trong database đã được bỏ 3 số 0 (VD: 2500000 -> 2500), đủ điều kiện >= 2000đ của PayOS
            int amount = booking.getTotalPrice().intValue();

            vn.payos.model.v2.paymentRequests.PaymentLinkItem item = vn.payos.model.v2.paymentRequests.PaymentLinkItem.builder()
                    .name("Thanh toan Booking #" + bookingId)
                    .quantity(1)
                    .price(Long.valueOf(amount))
                    .build();

            vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest paymentData = vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(Long.valueOf(amount))
                    .description("Thanh toan don " + bookingId)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .items(java.util.List.of(item))
                    .expiredAt(deadline != null
                            ? deadline.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
                            : null)
                    .build();

            vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);
            return data.getCheckoutUrl();
        } catch (Exception e) {
            throw new RuntimeException("Error creating PayOS payment link: " + e.getMessage(), e);
        }
    }

    /**
     * Doi chieu mot payment voi PayOS theo orderCode.
     *
     * Viec dich trang thai PayOS thanh trang thai payment/booking nam het o
     * PayOSReconciliationService.applyStatus - xem ly do o do. Ban sao truoc day
     * o day co dieu kien "PAID".equals(realStatus) || "PAID".equals(booking.getStatus()):
     * khach tao hai link cho cung mot don, tra qua link thu nhat, roi mo trang
     * ket qua cua link thu hai - link thu hai, chua he nhan dong nao, bi ghi
     * SUCCESS, trong khi no van con mo va van nhan tien duoc.
     */
    @Override
    @Transactional
    public PaymentResponse processPayOSCallback(java.util.Map<String, String> params) {
        String orderCodeStr = params.get("orderCode");

        if (orderCodeStr != null && !orderCodeStr.isEmpty()) {
            try {
                Payment payment = paymentRepository.findByOrderCode(orderCodeStr).orElse(null);

                if (payment != null) {
                    // Không tin tham số "status" do client gửi - luôn xác minh trạng thái thật với PayOS
                    long orderCode = Long.parseLong(orderCodeStr);
                    vn.payos.model.v2.paymentRequests.PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
                    reconciliationService.applyStatus(payment.getId(), paymentLink.getStatus().name());
                    return paymentMapper.toResponse(payment);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing PayOS callback: " + e.getMessage(), e);
            }
        }

        Payment dummy = new Payment();
        dummy.setPaymentStatus("FAILED");
        return paymentMapper.toResponse(dummy);
    }

    @Override
    @Transactional
    public void processPayOSWebhook(vn.payos.model.webhooks.Webhook webhookBody) {
        vn.payos.model.webhooks.WebhookData data;
        try {
            // Xac thuc chu ky: that bai o day la loi VINH VIEN (payload gia mao / sai dinh dang),
            // gui lai bao nhieu lan cung khong the thanh cong.
            data = payOS.webhooks().verify(webhookBody);
        } catch (Exception e) {
            throw new myproject.booking_tour.exception.WebhookVerificationException(
                    "Chu ky webhook PayOS khong hop le", e);
        }

        // Tu day tro di moi loi (DB, mang, PayOS API) deu la loi TAM THOI:
        // de exception nem ra ngoai de controller tra 5xx va PayOS retry.
        long orderCode = data.getOrderCode();
        Payment payment = paymentRepository.findByOrderCode(String.valueOf(orderCode)).orElse(null);
        if (payment == null) {
            return;
        }

        // Chi trang thai cua LINK moi noi duoc don da tra du hay chua. Truoc day
        // dieu kien la "PAID".equals(status) || "00".equals(data.getCode()), ma
        // "00" chi noi rang MOT lan chuyen khoan da vao: khach chuyen thieu thi
        // link o trang thai UNDERPAID, webhook van mang "00", va don duoc danh
        // dau PAID voi so tien chua du. Neu link chua kip chuyen sang PAID thi
        // /verify hoac luot doi soat 30 phut se ghi nhan sau.
        vn.payos.model.v2.paymentRequests.PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
        reconciliationService.applyStatus(payment.getId(), paymentLink.getStatus().name());
    }
}
