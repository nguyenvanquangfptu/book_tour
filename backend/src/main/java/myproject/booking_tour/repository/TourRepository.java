package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import myproject.booking_tour.dto.response.PopularDestinationResponse;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {
    List<Tour> findByStatus(String status);
    List<Tour> findByTitleContainingIgnoreCase(String keyword);
    List<Tour> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Tour> findByAvailableSlotsGreaterThan(Integer slots);

    @Query("SELECT new myproject.booking_tour.dto.response.PopularDestinationResponse(t.destination, COUNT(t.id), MAX(t.imageUrl)) " +
           "FROM Tour t WHERE t.destination IS NOT NULL AND t.destination != '' " +
           "GROUP BY t.destination ORDER BY COUNT(t.id) DESC")
    List<PopularDestinationResponse> findPopularDestinations(Pageable pageable);
}
