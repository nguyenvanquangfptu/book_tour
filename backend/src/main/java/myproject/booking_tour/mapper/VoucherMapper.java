package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.request.VoucherRequest;
import myproject.booking_tour.dto.response.VoucherResponse;
import myproject.booking_tour.entity.Voucher;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapper {
    public Voucher toEntity(VoucherRequest request) {
        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode());
        voucher.setDiscountAmount(request.getDiscountAmount());
        voucher.setDiscountPercentage(request.getDiscountPercentage());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setValidFrom(request.getValidFrom());
        voucher.setValidUntil(request.getValidUntil());
        if (request.getIsActive() != null) {
            voucher.setIsActive(request.getIsActive());
        }
        voucher.setUsageLimit(request.getUsageLimit());
        return voucher;
    }

    public VoucherResponse toResponse(Voucher voucher) {
        VoucherResponse response = new VoucherResponse();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setDiscountAmount(voucher.getDiscountAmount());
        response.setDiscountPercentage(voucher.getDiscountPercentage());
        response.setMaxDiscount(voucher.getMaxDiscount());
        response.setMinOrderValue(voucher.getMinOrderValue());
        response.setValidFrom(voucher.getValidFrom());
        response.setValidUntil(voucher.getValidUntil());
        response.setIsActive(voucher.getIsActive());
        response.setUsageLimit(voucher.getUsageLimit());
        response.setUsedCount(voucher.getUsedCount());
        return response;
    }

    public void updateEntityFromRequest(Voucher voucher, VoucherRequest request) {
        if (request.getCode() != null) voucher.setCode(request.getCode());
        voucher.setDiscountAmount(request.getDiscountAmount());
        voucher.setDiscountPercentage(request.getDiscountPercentage());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        if (request.getValidFrom() != null) voucher.setValidFrom(request.getValidFrom());
        if (request.getValidUntil() != null) voucher.setValidUntil(request.getValidUntil());
        if (request.getIsActive() != null) voucher.setIsActive(request.getIsActive());
        voucher.setUsageLimit(request.getUsageLimit());
    }
}
