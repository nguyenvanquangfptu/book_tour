package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.PaymentResponse;
import myproject.booking_tour.security.CustomUserDetails;
import myproject.booking_tour.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private CustomUserDetails getCurrentUserDetails() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        throw new myproject.booking_tour.exception.BadRequestException("User is not authenticated");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment invoice created successfully!", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        boolean isAdmin = "ADMIN".equals(userDetails.getUser().getRole().getName());
        PaymentResponse response = paymentService.getPaymentById(id, userDetails.getUser().getId(), isAdmin);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment invoice details retrieved successfully!", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        boolean isAdmin = "ADMIN".equals(userDetails.getUser().getRole().getName());
        List<PaymentResponse> list = paymentService.getAllPayments(userDetails.getUser().getId(), isAdmin);
        return ResponseEntity.ok(new ApiResponse<>(true, "All payment invoices retrieved successfully!", list));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        PaymentResponse response = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment status updated successfully!", response));
    }

    @GetMapping("/create-payos-url")
    public ResponseEntity<ApiResponse<String>> createPayOSUrl(
            @RequestParam Long bookingId,
            jakarta.servlet.http.HttpServletRequest request) {
        String paymentUrl = paymentService.createPaymentUrl(bookingId, request);
        if (paymentUrl != null && paymentUrl.startsWith("ERROR:")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, paymentUrl, null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "PayOS URL generated successfully!", paymentUrl));
    }

    @GetMapping("/payos-callback")
    public ResponseEntity<ApiResponse<PaymentResponse>> payOSCallback(
            @RequestParam java.util.Map<String, String> params) {
        PaymentResponse response = paymentService.processPayOSCallback(params);
        if ("SUCCESS".equals(response.getPaymentStatus())) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment successful!", response));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Payment failed or was cancelled!", response));
        }
    }
}
