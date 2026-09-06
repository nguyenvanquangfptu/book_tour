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
import myproject.booking_tour.service.BookingService;
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
    private final BookingService bookingService;
    private final vn.payos.PayOS payOS;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

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

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments(Long userId, boolean isAdmin) {
        return paymentRepository.findAll().stream()
                .filter(p -> isAdmin || (p.getBooking() != null && p.getBooking().getUser().getId().equals(userId)))
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

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getBooking().getId().equals(bookingId))
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }


    @org.springframework.beans.factory.annotation.Value("${payos.return-url}")
    private String returnUrl;

    @org.springframework.beans.factory.annotation.Value("${payos.cancel-url}")
    private String cancelUrl;

    @Override
    @Transactional
    public String createPaymentUrl(Long bookingId, jakarta.servlet.http.HttpServletRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

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
                    .build();

            vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);
            return data.getCheckoutUrl();
        } catch (Exception e) {
            throw new RuntimeException("Error creating PayOS payment link: " + e.getMessage(), e);
        }
    }

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
                    String realStatus = paymentLink.getStatus().name();

                    Booking booking = payment.getBooking();
                    if ("PAID".equals(realStatus) || "PAID".equals(booking.getStatus())) {
                        booking.setStatus("PAID");
                        bookingRepository.save(booking);

                        payment.setPaymentStatus("SUCCESS");
                        payment.setPaymentDate(LocalDateTime.now());
                        Payment saved = paymentRepository.save(payment);
                        return paymentMapper.toResponse(saved);
                    } else if ("CANCELLED".equals(realStatus) || "EXPIRED".equals(realStatus)) {
                        if (!"CANCELLED".equals(booking.getStatus())) {
                            bookingService.cancelBooking(booking.getId(), booking.getUser().getId());
                        }
                        payment.setPaymentStatus("FAILED");
                        payment.setPaymentDate(LocalDateTime.now());
                        Payment saved = paymentRepository.save(payment);
                        return paymentMapper.toResponse(saved);
                    }
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
        // Theo tài liệu PayOS, code "00" thường là thành công.
        // data.getOrderCode() returns long
        long orderCode = data.getOrderCode();
        String orderCodeStr = String.valueOf(orderCode);
        String desc = data.getDesc(); // Để biết lý do

        if (orderCodeStr != null && !orderCodeStr.isEmpty()) {
            Payment payment = paymentRepository.findByOrderCode(orderCodeStr).orElse(null);
            
            if (payment != null) {
                Booking booking = payment.getBooking();
                
                // Lấy trạng thái của giao dịch từ PayOS
                vn.payos.model.v2.paymentRequests.PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
                String status = paymentLink.getStatus().name();
                
                if ("PAID".equals(status) || "00".equals(data.getCode())) {
                    if (!"PAID".equals(booking.getStatus())) {
                        booking.setStatus("PAID");
                        bookingRepository.save(booking);
                    }
                    payment.setPaymentStatus("SUCCESS");
                    payment.setPaymentDate(LocalDateTime.now());
                    paymentRepository.save(payment);
                } else if ("CANCELLED".equals(status) || "EXPIRED".equals(status)) {
                    if (!"CANCELLED".equals(booking.getStatus())) {
                        bookingService.cancelBooking(booking.getId(), booking.getUser().getId());
                    }
                    payment.setPaymentStatus("FAILED");
                    payment.setPaymentDate(LocalDateTime.now());
                    paymentRepository.save(payment);
                }
            }
        }
    }
}
