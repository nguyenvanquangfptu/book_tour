package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.PaymentResponse;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse getPaymentById(Long id, Long userId, boolean isAdmin);
    PaymentResponse updatePaymentStatus(Long id, String status);
    List<PaymentResponse> getAllPayments(Long userId, boolean isAdmin);
    /**
     * @param userId  nguoi dang dang nhap - phai la chu cua booking
     * @param isAdmin admin duoc tao link ho khach
     */
    String createPaymentUrl(Long bookingId, Long userId, boolean isAdmin);
    PaymentResponse processPayOSCallback(Map<String, String> params);
    void processPayOSWebhook(vn.payos.model.webhooks.Webhook webhookBody);
    Long getPaymentOwnerUserIdByOrderCode(String orderCode);
}
