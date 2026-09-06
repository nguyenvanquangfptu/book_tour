package myproject.booking_tour.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Don cac refresh token da het han.
 *
 * Chi xoa theo expires_at, khong xoa theo revoked_at: mot token bi thu hoi vi
 * REUSE_DETECTED phai o lai den khi het han that su - do la dau vet duy nhat
 * cho thay tai khoan tung bi lam dung.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    // Chạy mỗi ngày vào lúc 00:00 (Nửa đêm)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("[CronJob] Bắt đầu dọn dẹp refresh token đã hết hạn...");

        try {
            int deleted = refreshTokenRepository.deleteExpired(LocalDateTime.now());
            log.info("[CronJob] Hoàn tất: đã xoá {} refresh token hết hạn.", deleted);
        } catch (Exception e) {
            log.error("[CronJob] Lỗi khi dọn dẹp refresh token: {}", e.getMessage());
        }
    }
}
