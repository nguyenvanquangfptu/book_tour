package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.TourSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

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

    @Autowired
    private TestEntityManager entityManager;

    private Tour testTour;
    private LocalDate departureDate;

    @BeforeEach
    void setUp() {
        testTour = new Tour();
        testTour.setTitle("Scheduled Tour");
        testTour.setPrice(BigDecimal.valueOf(100));
        tourRepository.save(testTour);

        departureDate = LocalDate.now().plusDays(5);

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(testTour);
        schedule.setDepartureDate(departureDate);
        schedule.setAvailableSlots(20);
        tourScheduleRepository.save(schedule);
    }

    /**
     * Cac cau UPDATE hang loat khong cap nhat persistence context, nen phai xoa
     * cache tang 1 truoc khi doc lai - neu khong se doc trung entity cu va test
     * "xanh" ma khong chung minh duoc gi.
     */
    private int slotsOf(LocalDate date) {
        entityManager.clear();
        return tourScheduleRepository.findFirstByTourIdAndDepartureDate(testTour.getId(), date)
                .map(TourSchedule::getAvailableSlots)
                .orElseThrow();
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

    @Test
    void insertIfAbsent_ShouldCreateRow_WhenDateHasNoScheduleYet() {
        LocalDate newDate = LocalDate.now().plusDays(30);

        int inserted = tourScheduleRepository.insertIfAbsent(testTour.getId(), newDate, 15);

        assertThat(inserted).isEqualTo(1);
        assertThat(slotsOf(newDate)).isEqualTo(15);
    }

    @Test
    void insertIfAbsent_ShouldDoNothing_WhenScheduleAlreadyExists() {
        // Day chinh la cuoc dua "hai nguoi cung dat mot ngay chua co dong lich":
        // nguoi thu hai khong duoc tao them dong nao, va tuyet doi khong duoc ghi
        // de so cho ve lai quota day du (20 chu khong phai 99).
        int inserted = tourScheduleRepository.insertIfAbsent(testTour.getId(), departureDate, 99);

        assertThat(inserted).isZero();
        assertThat(slotsOf(departureDate)).isEqualTo(20);
        entityManager.clear();
        assertThat(tourScheduleRepository.findAll()).hasSize(1);
    }

    @Test
    void deductSlots_ShouldDecrement_WhenEnoughSlots() {
        int updated = tourScheduleRepository.deductSlots(testTour.getId(), departureDate, 4);

        assertThat(updated).isEqualTo(1);
        assertThat(slotsOf(departureDate)).isEqualTo(16);
    }

    @Test
    void deductSlots_ShouldRefuseAndLeaveSlotsUntouched_WhenNotEnough() {
        int updated = tourScheduleRepository.deductSlots(testTour.getId(), departureDate, 21);

        assertThat(updated).isZero();
        assertThat(slotsOf(departureDate)).isEqualTo(20);
    }

    @Test
    void deductSlots_ShouldNeverOversell_WhenTwoBookingsRunBackToBack() {
        // Hai lan tru lien tiep, khong doc lai o giua - dung nhu hai request dong
        // thoi khi database da xep hang chung lai. Lan thu hai phai tinh tren so
        // cho CON LAI (5) chu khong phai so cho luc no bat dau (20).
        assertThat(tourScheduleRepository.deductSlots(testTour.getId(), departureDate, 15)).isEqualTo(1);
        assertThat(tourScheduleRepository.deductSlots(testTour.getId(), departureDate, 10)).isZero();

        assertThat(slotsOf(departureDate)).isEqualTo(5);
    }

    @Test
    void deductSlots_ShouldReturnZero_WhenDateHasNoSchedule() {
        int updated = tourScheduleRepository.deductSlots(testTour.getId(), LocalDate.now().plusDays(99), 1);

        assertThat(updated).isZero();
    }

    @Test
    void restoreSlots_ShouldGiveBackSlotsForEveryDayInRange() {
        LocalDate secondDay = departureDate.plusDays(1);
        LocalDate thirdDay = departureDate.plusDays(2);
        tourScheduleRepository.insertIfAbsent(testTour.getId(), secondDay, 20);
        tourScheduleRepository.insertIfAbsent(testTour.getId(), thirdDay, 20);
        for (LocalDate day : List.of(departureDate, secondDay, thirdDay)) {
            tourScheduleRepository.deductSlots(testTour.getId(), day, 6);
        }

        int restored = tourScheduleRepository.restoreSlots(testTour.getId(), departureDate, thirdDay, 6);

        assertThat(restored).isEqualTo(3);
        assertThat(slotsOf(departureDate)).isEqualTo(20);
        assertThat(slotsOf(secondDay)).isEqualTo(20);
        assertThat(slotsOf(thirdDay)).isEqualTo(20);
    }

    @Test
    void restoreSlots_ShouldNotTouchDaysOutsideTheRange() {
        LocalDate dayAfter = departureDate.plusDays(1);
        tourScheduleRepository.insertIfAbsent(testTour.getId(), dayAfter, 20);
        tourScheduleRepository.deductSlots(testTour.getId(), dayAfter, 8);

        tourScheduleRepository.restoreSlots(testTour.getId(), departureDate, departureDate, 8);

        assertThat(slotsOf(departureDate)).isEqualTo(28);
        assertThat(slotsOf(dayAfter)).isEqualTo(12);
    }
}
