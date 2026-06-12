package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Utility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilityRepository extends JpaRepository<Utility, Long> {
    Optional<Utility> findByName(String name);
    boolean existsByName(String name);
}
