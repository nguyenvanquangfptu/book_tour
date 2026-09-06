package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Ma OTP 6 so dung mot lan de dat lai mat khau, song 5 phut.
 *
 * Bang nay KHONG luu ma goc, chi luu SHA-256 cua no (xem TokenHasher) - cung
 * cach lam voi refresh_tokens. Ma goc chi ton tai trong email gui di.
 *
 * Ly do: mot ma OTP con han la doi duoc mat khau cua tai khoan tuong ung. Neu
 * luu tran, bat cu ai doc duoc database - ban sao luu bi lo, mot lo hong SQL
 * injection, hay chinh nguoi co quyen doc DB - deu chiem duoc tai khoan ma
 * khong can biet mat khau cu.
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 hex cua ma OTP - luon dung 64 ky tu. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public PasswordResetToken(String tokenHash, User user, LocalDateTime expiryDate) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiryDate = expiryDate;
    }
}
