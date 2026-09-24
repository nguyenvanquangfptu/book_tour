package myproject.booking_tour.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherResponse {
    private Long id;
    private String code;
    private BigDecimal discountAmount;
    private Double discountPercentage;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean isActive;
    private Integer usageLimit;
    private Integer usedCount;
}
