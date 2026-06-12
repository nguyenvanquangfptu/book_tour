package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.PaymentResponse;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse getPaymentById(Long id);
    PaymentResponse updatePaymentStatus(Long id, String status);
    List<PaymentResponse> getAllPayments();
    List<PaymentResponse> getPaymentsByBookingId(Long bookingId);
    String createPaymentUrl(Long bookingId, HttpServletRequest request);
    PaymentResponse processVnPayCallback(Map<String, String> params);
}
