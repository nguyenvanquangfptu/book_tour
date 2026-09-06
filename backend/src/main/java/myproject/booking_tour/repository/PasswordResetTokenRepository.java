package myproject.booking_tour.repository;

import myproject.booking_tour.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    /**
     * Tra theo SHA-256 cua ma OTP, khong phai ma goc. Ben goi phai bam truoc -
     * xem AuthServiceImpl.resetPassword(). Neu mot ben quen bam, viec doi mat
     * khau se im lang khong bao gio tim thay ma nao.
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteByUser(myproject.booking_tour.entity.User user);

    /**
     * Xoa cac ma OTP da het han.
     *
     * Truoc day khong ai don bang nay. deleteByUser() chi chay khi CHINH nguoi
     * do xin ma moi, nen ma cua nhung nguoi khong bao gio quay lai se nam do
     * mai mai. Bang chi lon dan, va no giu lai dau vet "tai khoan nay tung yeu
     * cau khoi phuc mat khau luc nao" - thong tin khong con phuc vu gi.
     *
     * @Transactional dat ngay tren phuong thuc de moi lenh don la mot giao dich
     * doc lap: TokenCleanupScheduler don nhieu bang, mot bang loi khong duoc
     * keo nhung bang khac xuong theo.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate <= :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
