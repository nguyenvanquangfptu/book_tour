package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByTour(Tour tour);
    List<Booking> findByStatus(String status);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByTourId(Long tourId);
}
