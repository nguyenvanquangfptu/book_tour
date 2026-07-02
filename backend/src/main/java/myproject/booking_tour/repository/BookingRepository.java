package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    @Override
    @EntityGraph(attributePaths = {"user", "tour"})
    Page<Booking> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "tour"})
    List<Booking> findAll();

    @EntityGraph(attributePaths = {"user", "tour"})
    List<Booking> findByUser(User user);

    @EntityGraph(attributePaths = {"user", "tour"})
    List<Booking> findByTour(Tour tour);

    @EntityGraph(attributePaths = {"user", "tour"})
    List<Booking> findByStatus(String status);

    @EntityGraph(attributePaths = {"user", "tour"})
    List<Booking> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "tour"})
    List<Booking> findByTourId(Long tourId);

    boolean existsByTourId(Long tourId);
}
