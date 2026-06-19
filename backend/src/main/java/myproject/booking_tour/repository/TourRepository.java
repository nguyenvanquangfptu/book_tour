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

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {

    @EntityGraph(attributePaths = {"accommodations", "utilities"})
    Page<Tour> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"accommodations", "utilities"})
    java.util.Optional<Tour> findById(Long id);

    @EntityGraph(attributePaths = {"accommodations", "utilities"})
    List<Tour> findAll();

    List<Tour> findByStatus(String status);
    List<Tour> findByTitleContainingIgnoreCaseAndStatusNot(String keyword, String status);
    List<Tour> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Tour> findByAvailableSlotsGreaterThan(Integer slots);

    @Query("SELECT new myproject.booking_tour.dto.response.PopularDestinationResponse(t.destination, COUNT(t.id), MAX(t.imageUrl)) " +
           "FROM Tour t WHERE t.destination IS NOT NULL AND t.destination != '' AND t.status = 'ACTIVE' " +
           "GROUP BY t.destination ORDER BY COUNT(t.id) DESC")
    List<PopularDestinationResponse> findPopularDestinations(Pageable pageable);

    @Query("SELECT DISTINCT t.destination FROM Tour t WHERE t.destination IS NOT NULL AND t.destination != '' AND t.status = 'ACTIVE'")
    List<String> findDistinctDestinations();

    @Query("SELECT DISTINCT t.tourType FROM Tour t WHERE t.tourType IS NOT NULL AND t.tourType != '' AND t.status = 'ACTIVE'")
    List<String> findDistinctTourTypes();

    @Query("SELECT DISTINCT t.transport FROM Tour t WHERE t.transport IS NOT NULL AND t.transport != '' AND t.status = 'ACTIVE'")
    List<String> findDistinctTransports();

    boolean existsByAccommodations_Id(Long accommodationId);
    List<Tour> findByAccommodations_Id(Long accommodationId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Tour t JOIN t.utilities u WHERE u.id = :utilityId")
    boolean existsByUtilityId(@org.springframework.data.repository.query.Param("utilityId") Long utilityId);
}
