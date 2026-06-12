package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {
    List<Tour> findByStatus(String status);
    List<Tour> findByTitleContainingIgnoreCase(String keyword);
    List<Tour> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Tour> findByAvailableSlotsGreaterThan(Integer slots);
}
