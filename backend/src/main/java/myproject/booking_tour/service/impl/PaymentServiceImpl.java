package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.PaymentResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
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

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentMapper paymentMapper;

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
            // Append 4 random digits to bookingId to make orderCode unique
            int randomSuffix = new java.util.Random().nextInt(9000) + 1000;
            long orderCode = Long.parseLong(bookingId.toString() + String.valueOf(randomSuffix));
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
            e.printStackTrace();
            return "ERROR:" + e.getMessage();
        }
    }

    @Override
    @Transactional
    public PaymentResponse processVnPayCallback(java.util.Map<String, String> params) {
        String orderCodeStr = params.get("orderCode");
        String status = params.get("status");
        
        if (orderCodeStr != null && orderCodeStr.length() > 4) {
            Long bookingId = Long.parseLong(orderCodeStr.substring(0, orderCodeStr.length() - 4));
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            
            if (booking != null) {
                if ("PAID".equals(status) || "PAID".equals(booking.getStatus())) {
                    booking.setStatus("PAID");
                    bookingRepository.save(booking);

                    Payment payment = paymentRepository.findAll().stream()
                        .filter(p -> p.getBooking().getId().equals(bookingId))
                        .findFirst().orElseGet(() -> {
                            Payment newPayment = new Payment();
                            newPayment.setBooking(booking);
                            newPayment.setAmount(booking.getTotalPrice());
                            newPayment.setPaymentMethod("PAYOS_BANK_TRANSFER");
                            return newPayment;
                        });
                    
                    payment.setPaymentStatus("SUCCESS");
                    payment.setPaymentDate(LocalDateTime.now());
                    Payment saved = paymentRepository.save(payment);
                    return paymentMapper.toResponse(saved);
                }
            }
        }
        
        Payment dummy = new Payment();
        dummy.setPaymentStatus("FAILED");
        return paymentMapper.toResponse(dummy);
    }
}
