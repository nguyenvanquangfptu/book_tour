package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
    List<Payment> findByPaymentStatus(String paymentStatus);
    Optional<Payment> findByOrderCode(String orderCode);

    List<Payment> findByPaymentStatusAndOrderCodeNotNull(String paymentStatus);

    /**
     * Hoa don cua rieng mot khach. Thay cho findAll() roi loc trong Java.
     */
    List<Payment> findByBookingUserId(Long userId);
}
