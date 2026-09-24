package myproject.booking_tour.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.repository.PasswordResetTokenRepository;
import myproject.booking_tour.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Don cac token da het han: refresh token va ma OTP khoi phuc mat khau.
 *
 * KHONG dat @Transactional tren phuong thuc nay. Moi lenh don la mot giao dich
 * doc lap (khai bao ngay tren phuong thuc cua repository), nen mot bang gap su
 * co khong lam mat ket qua cua bang kia. Day la cung bai hoc voi
 * PayOSReconciliationScheduler: mot @Transactional bao ca vong viec khien mot
 * loi le keo sap toan bo luot chay.
 *
 * Refresh token chi xoa theo expires_at, KHONG xoa theo revoked_at: mot token
 * bi thu hoi vi REUSE_DETECTED phai o lai den khi het han that su - do la dau
 * vet duy nhat cho thay tai khoan tung bi lam dung.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    // Chạy mỗi ngày vào lúc 00:00 (Nửa đêm)
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        try {
            int deleted = refreshTokenRepository.deleteExpired(now);
            log.info("[CronJob] Da xoa {} refresh token het han.", deleted);
        } catch (Exception e) {
            log.error("[CronJob] Loi khi don refresh token", e);
        }

        try {
            int deleted = passwordResetTokenRepository.deleteExpired(now);
            log.info("[CronJob] Da xoa {} ma OTP khoi phuc mat khau het han.", deleted);
        } catch (Exception e) {
            log.error("[CronJob] Loi khi don ma OTP", e);
        }
    }
}
