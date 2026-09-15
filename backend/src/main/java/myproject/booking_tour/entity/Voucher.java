package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "vouchers")
// Voucher co @Version, nen Hibernate bind HAI tham so cho cau xoa: id roi den
// version. Cau cu chi co mot dau "?" - lenh xoa nao cung no "column index out of
// range" o tham so thu hai, va nut Xoa o trang quan tri chua bao gio xoa duoc
// voucher nao. Dieu kien version con giu dung khoa lac quan: xoa mot voucher
// vua bi dat tour dung toi thi that bai thay vi ghi de.
@SQLDelete(sql = "UPDATE vouchers SET is_deleted = true WHERE id = ? AND version = ?")
@Where(clause = "is_deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Duy nhat trong so voucher CHUA XOA - index mot phan uq_vouchers_code_active
    // (V16). Khong khai bao unique o day: rang buoc toan bang tung chan viec tao
    // lai ma cua mot voucher da xoa.
    @Column(nullable = false, length = 50)
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

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
