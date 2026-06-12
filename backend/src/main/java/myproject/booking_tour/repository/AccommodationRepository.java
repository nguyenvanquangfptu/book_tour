package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    List<Accommodation> findByType(String type);
    List<Accommodation> findByNameContainingIgnoreCase(String keyword);
}
