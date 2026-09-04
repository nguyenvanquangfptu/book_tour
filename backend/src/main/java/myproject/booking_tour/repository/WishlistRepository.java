package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Wishlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * Wishlist cua mot nguoi, moi nhat truoc.
     *
     * @EntityGraph nap san "tour" trong cung mot query - neu de LAZY thi moi
     * dong lai sinh them mot query khi mapper doc thong tin tour (N+1).
     * Chi fetch mot quan he ManyToOne nen khong gap van de tich Descartes nhu
     * khi fetch nhieu collection cung luc.
     */
    @EntityGraph(attributePaths = {"tour"})
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndTourId(Long userId, Long tourId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId AND w.tour.id = :tourId")
    int deleteByUserIdAndTourId(@Param("userId") Long userId, @Param("tourId") Long tourId);
}
