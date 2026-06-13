package myproject.booking_tour.controller;

import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/payos_transfer_handler")
@CrossOrigin(origins = "*")
public class PayOSWebhookController {

    @Autowired
    private PayOS payOS;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @PostMapping
    public ResponseEntity<Map<String, String>> handlePayOSWebhook(@RequestBody Object body) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String jsonBody = mapper.writeValueAsString(body);
            
            Webhook webhookBody = mapper.readValue(jsonBody, Webhook.class);
            WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);
            
            // data.getOrderCode() returns long
            long orderCode = data.getOrderCode();
            String orderCodeStr = String.valueOf(orderCode);
            
            if (orderCodeStr.length() > 4) {
                Long bookingId = Long.parseLong(orderCodeStr.substring(0, orderCodeStr.length() - 4));
                
                Booking booking = bookingRepository.findById(bookingId).orElse(null);
                if (booking != null) {
                    booking.setStatus("CONFIRMED");
                    bookingRepository.save(booking);
                    
                    Payment payment = new Payment();
                    payment.setBooking(booking);
                    payment.setAmount(booking.getTotalPrice());
                    payment.setPaymentMethod("PAYOS_BANK_TRANSFER");
                    payment.setPaymentStatus("SUCCESS");
                    payment.setPaymentDate(LocalDateTime.now());
                    paymentRepository.save(payment);
                }
            }
            
            return ResponseEntity.ok(Map.of("success", "true"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", "false"));
        }
    }
}
