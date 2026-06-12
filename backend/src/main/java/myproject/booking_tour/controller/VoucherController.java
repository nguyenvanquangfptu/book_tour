package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.VoucherRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.VoucherResponse;
import myproject.booking_tour.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAllVouchers() {
        List<VoucherResponse> vouchers = voucherService.getAllVouchers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Vouchers retrieved successfully!", vouchers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(@PathVariable Long id) {
        VoucherResponse voucher = voucherService.getVoucherById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Voucher details retrieved successfully!", voucher));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherByCode(@PathVariable String code) {
        VoucherResponse voucher = voucherService.getVoucherByCode(code);
        return ResponseEntity.ok(new ApiResponse<>(true, "Voucher details retrieved successfully!", voucher));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(@Valid @RequestBody VoucherRequest request) {
        VoucherResponse voucher = voucherService.createVoucher(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Voucher created successfully!", voucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(@PathVariable Long id, @Valid @RequestBody VoucherRequest request) {
        VoucherResponse voucher = voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Voucher updated successfully!", voucher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Voucher deleted successfully!", null));
    }
}
