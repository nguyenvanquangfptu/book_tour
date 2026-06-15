package myproject.booking_tour.repository;

import myproject.booking_tour.entity.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    Optional<TourSchedule> findByTourIdAndDepartureDate(Long tourId, LocalDate departureDate);
}
