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

    // KHONG dung @EntityGraph fetch dong thoi "accommodations" (Set) va "utilities" (List):
    // SQL sinh ra tich Descartes |accommodations| x |utilities|. Hibernate khu trung lap cho Set,
    // nhung List (bag) thi khong -> moi utility bi lap lai |accommodations| lan.
    // Ngoai ra fetch collection kem Pageable con gay HHH000104 (phan trang trong bo nho).
    //
    // Thay bang hibernate.default_batch_fetch_size trong application.properties: Hibernate gom
    // viec load collection cua N tour thanh 1 query "WHERE tour_id IN (?,?,...)" -> het N+1,
    // khong sinh tich Descartes, va ap dung cho MOI duong dan truy van (ke ca findAll(spec, pageable)
    // cua JpaSpecificationExecutor - noi @EntityGraph khong the voi toi).

    java.util.Optional<Tour> findBySlug(String slug);

    boolean existsBySlug(String slug);

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

    // Native query de lach @SQLRestriction("is_deleted = false") tren entity Tour.
    // Vi Hibernate KHONG viet lai SQL cua native query, no cung khong tu chen 3
    // field @Formula (bookedCount, reviewCount, rating) vao SELECT nhu voi JPQL
    // -> phai tu viet ra day, neu khong se loi "column bookedCount not found".
    // Alias dat trong dau nhay kep de PostgreSQL giu nguyen camelCase, khop dung
    // ten property. Khong trich dan thi Postgres ha thanh bookedcount va phai
    // trong cay vao viec ResultSet.findColumn cua driver tra cuu khong phan biet
    // hoa thuong - dung duoc nhung mong manh, khong nen dua vao.
    @Query(value = """
            SELECT t.*,
              (SELECT COALESCE(SUM(b.number_of_people), 0) FROM bookings b WHERE b.tour_id = t.id) AS "bookedCount",
              (SELECT COUNT(r.id) FROM reviews r WHERE r.tour_id = t.id) AS "reviewCount",
              (SELECT COALESCE(AVG(CAST(r.rating AS DOUBLE PRECISION)), 0) FROM reviews r WHERE r.tour_id = t.id) AS "rating"
            FROM tours t
            WHERE t.is_deleted = true
            """, nativeQuery = true)
    List<Tour> findDeletedTours();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "UPDATE tours SET is_deleted = false WHERE id = :tourId", nativeQuery = true)
    void restoreTour(@org.springframework.data.repository.query.Param("tourId") Long tourId);
}
