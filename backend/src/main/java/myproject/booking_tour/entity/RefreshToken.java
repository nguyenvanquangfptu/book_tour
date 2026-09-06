package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mot refresh token - chuoi ngau nhien 256-bit song 30 ngay, doi lay access
 * token moi khi cai cu het han.
 *
 * CHI DUNG DUOC MOT LAN. Moi lan lam moi, token nay bi danh dau usedAt va mot
 * token moi ra doi trong cung {@link #familyId}. Neu no xuat hien lan thu hai
 * thi da bi sao chep - xem RefreshTokenService.rotate().
 *
 * Khong dung @Data cua Lombok: quan he ManyToOne LAZY se bi equals/hashCode va
 * toString tu sinh keo ra khoi proxy (giong Wishlist).
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    /** Ly do mot token khong con hieu luc. Null nghia la van dung duoc. */
    public enum RevocationReason {
        /** Da duoc dung de lam moi va sinh ra token ke tiep - vong doi binh thuong. */
        ROTATED,
        /** Nguoi dung bam dang xuat. */
        LOGOUT,
        /** Token da tieu lai duoc dung lan nua -> ca family bi thu hoi. */
        REUSE_DETECTED,
        /** Doi mat khau: moi phien cu deu phai dang nhap lai. */
        PASSWORD_RESET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 hex cua token. Ban goc chi ton tai trong cookie cua trinh duyet. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Mot lan dang nhap = mot family. Don vi bi thu hoi khi phat hien dung lai. */
    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    /** Token da sinh ra token nay - chi de truy vet nguoc chuoi xoay vong. */
    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    /**
     * Han tuyet doi, tinh tu luc DANG NHAP chu khong phai tu luc token nay sinh
     * ra: moi token trong cung family deu mang chung mot gia tri. Xoay vong
     * khong gia han, nen mot phien khong the song mai.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** != null nghia la da tieu. Day chinh la co de phat hien dung lai. */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason", length = 30)
    private RevocationReason revokedReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /**
     * Da tung duoc mang di doi lay token moi.
     *
     * Chi RIENG co nay moi la bang chung token bi sao chep - mot token da tieu
     * ma quay lai nghia la ban goc cua no ton tai o hai noi. Phai tach bach voi
     * {@link #isRevoked()}: token bi thu hoi do logout hay doi mat khau la do
     * CHINH HE THONG lam, khong phai dau hieu tan cong.
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /** Khong con dung duoc, vi bat ky ly do nao trong {@link RevocationReason}. */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpiredAt(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }
}
