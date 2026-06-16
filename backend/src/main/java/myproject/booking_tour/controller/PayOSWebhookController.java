package myproject.booking_tour.controller;

import myproject.booking_tour.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/payos_transfer_handler")
@CrossOrigin(origins = "*")
public class PayOSWebhookController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private vn.payos.PayOS payOS;

    @PostMapping
    public ResponseEntity<Map<String, String>> handlePayOSWebhook(@RequestBody Object body) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String jsonBody = mapper.writeValueAsString(body);
            
            Webhook webhookBody = mapper.readValue(jsonBody, Webhook.class);
            
            // Delegate logic to PaymentService where signature is verified and cancellation handled
            paymentService.processPayOSWebhook(webhookBody);
            
            return ResponseEntity.ok(Map.of("success", "true"));
        } catch (Exception e) {
            throw new myproject.booking_tour.exception.BadRequestException("Lỗi webhook PayOS: " + e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestParam String orderCode) {
        try {
            long orderCodeLong = Long.parseLong(orderCode);
            vn.payos.model.v2.paymentRequests.PaymentLink paymentLink = payOS.paymentRequests().get(orderCodeLong);

            if ("PAID".equals(paymentLink.getStatus().name())) {
                // Call processPayOSCallback manually to share logic
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("orderCode", orderCode);
                params.put("status", "PAID");
                paymentService.processPayOSCallback(params);
                return ResponseEntity.ok(Map.of("success", "true", "status", "PAID"));
            } else {
                return ResponseEntity.ok(Map.of("success", "false", "status", paymentLink.getStatus().name()));
            }
        } catch (Exception e) {
            throw new myproject.booking_tour.exception.BadRequestException("Lỗi xác minh thanh toán: " + e.getMessage());
        }
    }
}
