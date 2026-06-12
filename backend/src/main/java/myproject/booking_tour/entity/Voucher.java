package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column
    private Double discountPercentage;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxDiscount;

    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderValue;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Integer usageLimit;

    @Column(nullable = false)
    private Integer usedCount = 0;
}
