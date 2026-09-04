package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.TourSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TourScheduleRepositoryTest {

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private TourRepository tourRepository;

    private Tour testTour;

    @BeforeEach
    void setUp() {
        testTour = new Tour();
        testTour.setTitle("Scheduled Tour");
        testTour.setPrice(BigDecimal.valueOf(100));
        tourRepository.save(testTour);

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(testTour);
        schedule.setDepartureDate(LocalDate.now().plusDays(5));
        schedule.setAvailableSlots(20);
        tourScheduleRepository.save(schedule);
    }

    @Test
    void findFirstByTourIdAndDepartureDate_ShouldReturnSchedule() {
        Optional<TourSchedule> result = tourScheduleRepository.findFirstByTourIdAndDepartureDate(testTour.getId(), LocalDate.now().plusDays(5));
        assertThat(result).isPresent();
    }

    @Test
    void findByTourIdAndDepartureDateBetween_ShouldReturnSchedules() {
        List<TourSchedule> result = tourScheduleRepository.findByTourIdAndDepartureDateBetween(
                testTour.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );
        assertThat(result).hasSize(1);
    }

    @Test
    void deleteByTourId_ShouldRemoveSchedules() {
        tourScheduleRepository.deleteByTourId(testTour.getId());
        List<TourSchedule> result = tourScheduleRepository.findAll();
        assertThat(result).isEmpty();
    }
}
