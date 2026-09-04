package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TourRepository tourRepository;

    private User testUser;
    private Tour testTour;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("ROLE_USER");
        roleRepository.save(role);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword("pass");
        testUser.setRole(role);
        userRepository.save(testUser);

        testTour = new Tour();
        testTour.setTitle("Tour 1");
        testTour.setStatus("ACTIVE");
        testTour.setPrice(BigDecimal.valueOf(100));
        tourRepository.save(testTour);

        Booking booking = new Booking();
        booking.setUser(testUser);
        booking.setTour(testTour);
        booking.setStatus("PENDING");
        booking.setNumberOfPeople(2);
        booking.setTotalPrice(BigDecimal.valueOf(200));
        bookingRepository.save(booking);
    }

    @Test
    void findByUser_ShouldReturnBookings_WhenUserMatches() {
        List<Booking> result = bookingRepository.findByUser(testUser);
        assertThat(result).hasSize(1);
    }

    @Test
    void findByTour_ShouldReturnBookings_WhenTourMatches() {
        List<Booking> result = bookingRepository.findByTour(testTour);
        assertThat(result).hasSize(1);
    }

    @Test
    void findByStatus_ShouldReturnBookings_WhenStatusMatches() {
        List<Booking> result = bookingRepository.findByStatus("PENDING");
        assertThat(result).hasSize(1);
    }
}
