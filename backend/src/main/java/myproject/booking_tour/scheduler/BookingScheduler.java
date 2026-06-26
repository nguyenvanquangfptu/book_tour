package myproject.booking_tour.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.repository.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    // Chạy mỗi phút 1 lần để dễ test. Có thể đổi thành "0 0 * * * *" (mỗi giờ) trên production.
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void cancelUnpaidBookings() {
        log.info("Running job to cancel unpaid APPROVED bookings older than 24 hours...");
        List<Booking> approvedBookings = bookingRepository.findByStatus("APPROVED");
        
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        
        for (Booking booking : approvedBookings) {
            LocalDateTime approvedTime = booking.getApprovedAt();
            if (approvedTime == null) {
                // Fallback to bookingDate if approvedAt is null
                approvedTime = booking.getBookingDate();
            }
            
            if (approvedTime != null && approvedTime.plusHours(24).isBefore(now)) {
                booking.setStatus("CANCELLED");
                bookingRepository.save(booking);
                count++;
                log.info("Cancelled booking ID {} due to unpaid after 24 hours.", booking.getId());
            }
        }
        
        if (count > 0) {
            log.info("Successfully cancelled {} unpaid bookings.", count);
        }
    }
}
