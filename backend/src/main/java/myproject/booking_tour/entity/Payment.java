package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ref: payments.booking_id > bookings.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * Ma don hang ben PayOS. Truoc day duoc nhet chung vao paymentMethod duoi
     * dang "PAYOS_<orderCode>" khien khong the dat UNIQUE va khong index duoc.
     * Cot nay co rang buoc UNIQUE - do chinh la thu bao dam webhook idempotent.
     */
    @Column(name = "order_code", length = 50, unique = true)
    private String orderCode;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @CreationTimestamp
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
}
