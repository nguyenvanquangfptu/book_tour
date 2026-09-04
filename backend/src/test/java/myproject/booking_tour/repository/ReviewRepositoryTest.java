package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Review;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private Tour testTour;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("ROLE_USER");
        roleRepository.save(role);

        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword("pass");
        testUser.setRole(role);
        userRepository.save(testUser);

        testTour = new Tour();
        testTour.setTitle("Reviewed Tour");
        testTour.setPrice(BigDecimal.valueOf(100));
        tourRepository.save(testTour);

        Review review = new Review();
        review.setTour(testTour);
        review.setUser(testUser);
        review.setRating(5);
        review.setComment("Great!");
        reviewRepository.save(review);
    }

    @Test
    void findByTourId_ShouldReturnReviews() {
        List<Review> result = reviewRepository.findByTourId(testTour.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRating()).isEqualTo(5);
    }
}
