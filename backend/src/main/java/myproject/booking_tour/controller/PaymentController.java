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

    // GET /payos-callback da duoc go bo.
    //
    // Endpoint do nhan orderCode tu query string va hanh dong ngay, KHONG he doc
    // toi danh tinh nguoi goi - trong khi /api/payments/** chi yeu cau co mot tai
    // khoan bat ky. Bat cu khach hang nao biet orderCode cua nguoi khac deu doc
    // duoc so tien, thoi diem, trang thai don hang do, va kich hoat duoc viec huy
    // booking cua ho (processPayOSCallback truyen chinh id chu booking vao
    // cancelBooking, nen chot kiem tra quyen ben trong bi thoa man hinh thuc).
    //
    // orderCode khong phai bi mat: no nam trong URL PayOS tra ve, trong lich su
    // trinh duyet va header Referer. No con doan duoc, vi duoc sinh theo cong
    // thuc <giay epoch><id tuan tu> - xem PaymentServiceImpl.createPaymentUrl.
    //
    // Khong them kiem tra quyen ma xoa han, vi endpoint nay khong con ai dung:
    // trang PaymentResult goi no la trang mo coi, khong co duong nao dan toi.
    // Duong xac nhan dang chay that la GET /api/payment/payos_transfer_handler/verify
    // - endpoint do CO doi chieu chu so huu.
    //
    // PaymentService.processPayOSCallback duoc GIU LAI: /verify van goi no.
}
