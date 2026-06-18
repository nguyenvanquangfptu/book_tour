package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.PaymentResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
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

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private BookingService bookingService;

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
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment invoice not found with id: " + id));
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
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getBooking().getId().equals(bookingId))
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Autowired
    private vn.payos.PayOS payOS;

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
            payment.setPaymentMethod("PAYOS_BANK_TRANSFER");
            payment.setPaymentStatus("PENDING");
            payment.setPaymentDate(LocalDateTime.now());
            Payment savedPayment = paymentRepository.save(payment);

            // Use the saved payment ID as the unique orderCode for PayOS
            long orderCode = savedPayment.getId();

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
        String status = params.get("status");
        
        if (orderCodeStr != null && !orderCodeStr.isEmpty()) {
            try {
                Long paymentId = Long.parseLong(orderCodeStr);
                Payment payment = paymentRepository.findById(paymentId).orElse(null);
                
                if (payment != null) {
                    Booking booking = payment.getBooking();
                    if ("PAID".equals(status) || "PAID".equals(booking.getStatus())) {
                        booking.setStatus("PAID");
                        bookingRepository.save(booking);

                        payment.setPaymentStatus("SUCCESS");
                        payment.setPaymentDate(LocalDateTime.now());
                        Payment saved = paymentRepository.save(payment);
                        return paymentMapper.toResponse(saved);
                    } else if ("CANCELLED".equals(status)) {
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

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 1800000) // 30 minutes
    @Transactional
    public void checkPendingPayOSPayments() {
        System.out.println("[CronJob] Bắt đầu kiểm tra các giao dịch PayOS đang chờ...");
        
        // Find all payments that are PENDING and use PAYOS_BANK_TRANSFER
        List<Payment> pendingPayments = paymentRepository.findAll().stream()
                .filter(p -> "PENDING".equals(p.getPaymentStatus()) && "PAYOS_BANK_TRANSFER".equals(p.getPaymentMethod()))
                .collect(Collectors.toList());

        for (Payment payment : pendingPayments) {
            try {
                long orderCode = payment.getId();
                vn.payos.model.v2.paymentRequests.PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);

                if ("PAID".equals(paymentLink.getStatus().name())) {
                    System.out.println("[CronJob] Tìm thấy đơn hàng đã thanh toán: PaymentID=" + orderCode);
                    
                    Booking booking = payment.getBooking();
                    if (!"PAID".equals(booking.getStatus())) {
                        booking.setStatus("PAID");
                        bookingRepository.save(booking);
                    }
                    
                    payment.setPaymentStatus("SUCCESS");
                    payment.setPaymentDate(LocalDateTime.now());
                    paymentRepository.save(payment);
                } else if ("CANCELLED".equals(paymentLink.getStatus().name()) || "EXPIRED".equals(paymentLink.getStatus().name())) {
                    System.out.println("[CronJob] Đơn hàng đã hủy/hết hạn: PaymentID=" + orderCode);
                    Booking booking = payment.getBooking();
                    if (!"CANCELLED".equals(booking.getStatus())) {
                        bookingService.cancelBooking(booking.getId(), booking.getUser().getId());
                    }
                    payment.setPaymentStatus("FAILED");
                    payment.setPaymentDate(LocalDateTime.now());
                    paymentRepository.save(payment);
                }
            } catch (Exception e) {
                System.err.println("[CronJob] Lỗi kiểm tra PaymentID=" + payment.getId() + ": " + e.getMessage());
            }
        }
        System.out.println("[CronJob] Hoàn tất kiểm tra.");
    }

    @Override
    @Transactional
    public void processPayOSWebhook(vn.payos.model.webhooks.Webhook webhookBody) {
        try {
            vn.payos.model.webhooks.WebhookData data = payOS.webhooks().verify(webhookBody);
            
            // Theo tài liệu PayOS, code "00" thường là thành công.
            // data.getOrderCode() returns long
            long orderCode = data.getOrderCode();
            String orderCodeStr = String.valueOf(orderCode);
            String desc = data.getDesc(); // Để biết lý do

            if (orderCodeStr != null && !orderCodeStr.isEmpty()) {
                Long paymentId = Long.parseLong(orderCodeStr);
                Payment payment = paymentRepository.findById(paymentId).orElse(null);
                
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
        } catch (Exception e) {
            throw new myproject.booking_tour.exception.BadRequestException("Lỗi xác thực webhook PayOS: " + e.getMessage());
        }
    }
}
