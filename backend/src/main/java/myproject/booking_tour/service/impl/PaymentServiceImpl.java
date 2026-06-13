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

    @org.springframework.beans.factory.annotation.Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @org.springframework.beans.factory.annotation.Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @org.springframework.beans.factory.annotation.Value("${vnpay.url}")
    private String vnp_PayUrl;

    @org.springframework.beans.factory.annotation.Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Override
    @Transactional
    public String createPaymentUrl(Long bookingId, jakarta.servlet.http.HttpServletRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = myproject.booking_tour.config.VNPayConfig.getRandomNumber(8) + "_" + bookingId;
        String vnp_IpAddr = myproject.booking_tour.config.VNPayConfig.getIpAddress(request);
        
        long amount = booking.getTotalPrice().longValue() * 100;
        java.util.Map<String, String> vnp_Params = new java.util.HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + bookingId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        java.util.Calendar cld = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(java.util.Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        java.util.List<String> fieldNames = new java.util.ArrayList<>(vnp_Params.keySet());
        java.util.Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        java.util.Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(java.net.URLEncoder.encode(fieldName, java.nio.charset.StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII.toString()));
                } catch (java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = myproject.booking_tour.config.VNPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnp_PayUrl + "?" + queryUrl;
    }

    @Override
    @Transactional
    public PaymentResponse processVnPayCallback(java.util.Map<String, String> params) {
        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TransactionNo = params.get("vnp_TransactionNo");
        
        Long bookingId = Long.parseLong(vnp_TxnRef.split("_")[1]);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod("VNPAY");
        payment.setPaymentDate(LocalDateTime.now());

        if ("00".equals(vnp_ResponseCode)) {
            payment.setPaymentStatus("SUCCESS");
            booking.setStatus("CONFIRMED"); // update booking status
        } else {
            payment.setPaymentStatus("FAILED");
        }
        
        bookingRepository.save(booking);
        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }
}
