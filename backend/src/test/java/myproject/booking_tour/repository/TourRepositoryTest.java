package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.Accommodation;
import myproject.booking_tour.dto.response.PopularDestinationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TourRepositoryTest {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    private Tour testTour;

    @BeforeEach
    void setUp() {
        testTour = new Tour();
        testTour.setTitle("Test Tour");
        testTour.setSlug("test-tour");
        testTour.setDestination("Hanoi");
        testTour.setTourType("Adventure");
        testTour.setTransport("Bus");
        testTour.setPrice(BigDecimal.valueOf(100));
        testTour.setAvailableSlots(10);
        testTour.setStatus("ACTIVE");
        testTour.setIsDeleted(false);
        tourRepository.save(testTour);
    }

    @Test
    void findBySlug_ShouldReturnTour_WhenExists() {
        Optional<Tour> result = tourRepository.findBySlug("test-tour");
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Tour");
    }

    @Test
    void existsBySlug_ShouldReturnTrue_WhenExists() {
        boolean exists = tourRepository.existsBySlug("test-tour");
        assertThat(exists).isTrue();
    }

    @Test
    void findByStatus_ShouldReturnTours_WhenMatch() {
        List<Tour> result = tourRepository.findByStatus("ACTIVE");
        assertThat(result).isNotEmpty();
    }

    @Test
    void findByPriceBetween_ShouldReturnTours_WhenMatch() {
        List<Tour> result = tourRepository.findByPriceBetween(BigDecimal.valueOf(50), BigDecimal.valueOf(150));
        assertThat(result).isNotEmpty();
    }

    @Test
    void findPopularDestinations_ShouldReturnGroupedDestinations() {
        List<PopularDestinationResponse> result = tourRepository.findPopularDestinations(PageRequest.of(0, 10));
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Hanoi");
    }

    @Test
    void findDistinctDestinations_ShouldReturnUniqueDestinations() {
        List<String> result = tourRepository.findDistinctDestinations();
        assertThat(result).contains("Hanoi");
    }

    @Test
    void restoreTour_ShouldUpdateIsDeletedToFalse() {
        testTour.setIsDeleted(true);
        tourRepository.save(testTour);
        
        tourRepository.restoreTour(testTour.getId());
        
        Tour restored = tourRepository.findById(testTour.getId()).orElseThrow();
        assertThat(restored.getIsDeleted()).isFalse();
    }
}
