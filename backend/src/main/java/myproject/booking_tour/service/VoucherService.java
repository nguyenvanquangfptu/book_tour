package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.VoucherRequest;
import myproject.booking_tour.dto.response.VoucherResponse;

import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getAllVouchers();
    VoucherResponse getVoucherById(Long id);
    VoucherResponse getVoucherByCode(String code);
    VoucherResponse createVoucher(VoucherRequest request);
    VoucherResponse updateVoucher(Long id, VoucherRequest request);
    void deleteVoucher(Long id);
}
