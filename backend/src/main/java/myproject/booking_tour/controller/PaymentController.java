package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.PaymentResponse;
import myproject.booking_tour.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment invoice created successfully!", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment invoice details retrieved successfully!", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        List<PaymentResponse> list = paymentService.getAllPayments();
        return ResponseEntity.ok(new ApiResponse<>(true, "All payment invoices retrieved successfully!", list));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        PaymentResponse response = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment status updated successfully!", response));
    }

    @GetMapping("/create-vnpay-url")
    public ResponseEntity<ApiResponse<String>> createVnPayUrl(
            @RequestParam Long bookingId,
            jakarta.servlet.http.HttpServletRequest request) {
        String paymentUrl = paymentService.createPaymentUrl(bookingId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "VNPay URL generated successfully!", paymentUrl));
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<ApiResponse<PaymentResponse>> vnPayCallback(
            @RequestParam java.util.Map<String, String> params) {
        PaymentResponse response = paymentService.processVnPayCallback(params);
        if ("SUCCESS".equals(response.getPaymentStatus())) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment successful!", response));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Payment failed or was cancelled!", response));
        }
    }
}
