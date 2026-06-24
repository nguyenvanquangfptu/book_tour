package myproject.booking_tour.repository;

import myproject.booking_tour.entity.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    Optional<TourSchedule> findFirstByTourIdAndDepartureDate(Long tourId, LocalDate departureDate);

    java.util.List<TourSchedule> findByTourIdAndDepartureDateBetween(Long tourId, LocalDate startDate, LocalDate endDate);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM TourSchedule ts WHERE ts.tour.id = :tourId")
    void deleteByTourId(@org.springframework.data.repository.query.Param("tourId") Long tourId);
}
