package myproject.booking_tour.repository;

import myproject.booking_tour.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    
    @Modifying
    @Query("DELETE FROM InvalidatedToken t WHERE t.expiryTime <= :now")
    void deleteAllExpiredSince(Date now);
}
