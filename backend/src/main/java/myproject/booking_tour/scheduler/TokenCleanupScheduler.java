package myproject.booking_tour.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.repository.InvalidatedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    // Chạy mỗi ngày vào lúc 00:00 (Nửa đêm)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("[CronJob] Bắt đầu dọn dẹp các token đã hết hạn trong Blacklist...");
        
        try {
            invalidatedTokenRepository.deleteAllExpiredSince(new Date());
            log.info("[CronJob] Hoàn tất dọn dẹp token rác trong Blacklist.");
        } catch (Exception e) {
            log.error("[CronJob] Lỗi khi dọn dẹp token rác: {}", e.getMessage());
        }
    }
}
