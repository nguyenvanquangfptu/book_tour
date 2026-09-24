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
    void isSlugTaken_ShouldReturnTrue_WhenExists() {
        assertThat(tourRepository.isSlugTaken("test-tour")).isTrue();
        assertThat(tourRepository.isSlugTaken("khong-ton-tai")).isFalse();
    }

    /**
     * uk_slug ap dung cho moi dong, ke ca tour trong thung rac. Kiem tra ma bo
     * qua tour da xoa thi slug sinh ra dung rang buoc luc INSERT.
     */
    @Test
    void isSlugTaken_ShouldCountToursInTheTrash() {
        tourRepository.delete(testTour);
        tourRepository.flush();

        assertThat(tourRepository.findBySlug("test-tour")).isEmpty();
        assertThat(tourRepository.isSlugTaken("test-tour")).isTrue();
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

    @Autowired
    private UtilityRepository utilityRepository;

    /**
     * tour_utilities khong co ON DELETE CASCADE: tien ich con gan voi tour
     * trong thung rac ma bi coi la "khong ai dung" thi lenh DELETE dung khoa
     * ngoai va thanh 500.
     */
    @Test
    void existsByUtilityId_ShouldCountToursInTheTrash() {
        myproject.booking_tour.entity.Utility wifi = new myproject.booking_tour.entity.Utility();
        wifi.setName("Wifi");
        utilityRepository.save(wifi);
        testTour.getUtilities().add(wifi);
        tourRepository.saveAndFlush(testTour);

        tourRepository.softDelete(testTour.getId());

        assertThat(tourRepository.existsByUtilityId(wifi.getId())).isTrue();
    }

    /**
     * repository.delete(tour) xoa cac dong noi truoc khi chay @SQLDelete: tour
     * khoi phuc tu thung rac mat het noi luu tru va tien ich.
     */
    @Test
    void softDeleteThenRestore_ShouldBringTheTourBackWithItsAccommodationsAndUtilities() {
        Accommodation hotel = new Accommodation();
        hotel.setName("Khach san A");
        hotel.setType("HOTEL");
        hotel.setAddress("Ha Noi");
        accommodationRepository.save(hotel);
        myproject.booking_tour.entity.Utility wifi = new myproject.booking_tour.entity.Utility();
        wifi.setName("Wifi");
        utilityRepository.save(wifi);
        testTour.getAccommodations().add(hotel);
        testTour.getUtilities().add(wifi);
        tourRepository.saveAndFlush(testTour);

        tourRepository.softDelete(testTour.getId());
        assertThat(tourRepository.findById(testTour.getId())).isEmpty();
        assertThat(tourRepository.existsByAccommodations_Id(hotel.getId())).isTrue();

        tourRepository.restoreTour(testTour.getId());

        Tour restored = tourRepository.findById(testTour.getId()).orElseThrow();
        assertThat(restored.getAccommodations()).extracting(Accommodation::getId).containsExactly(hotel.getId());
        assertThat(restored.getUtilities()).extracting(myproject.booking_tour.entity.Utility::getId).containsExactly(wifi.getId());
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
