package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    @NotBlank(message = "Voucher code is required")
    private String code;

    private BigDecimal discountAmount;
    private Double discountPercentage;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;

    private Boolean isActive = true;
    private Integer usageLimit;
}
