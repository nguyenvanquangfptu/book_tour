package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Accommodation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccommodationRepositoryTest {

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Test
    void findByType_ShouldReturnAccommodations_WhenTypeMatches() {
        // Arrange
        Accommodation acc1 = new Accommodation();
        acc1.setName("Hotel A");
        acc1.setType("Hotel");
        acc1.setAddress("1 Hotel Street");
        accommodationRepository.save(acc1);

        Accommodation acc2 = new Accommodation();
        acc2.setName("Resort B");
        acc2.setType("Resort");
        acc2.setAddress("2 Resort Avenue");
        accommodationRepository.save(acc2);

        // Act
        List<Accommodation> result = accommodationRepository.findByType("Hotel");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hotel A");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnAccommodations_WhenNameMatches() {
        // Arrange
        Accommodation acc1 = new Accommodation();
        acc1.setName("Beautiful Resort in Bali");
        acc1.setType("Resort");
        acc1.setAddress("1 Bali Street");
        accommodationRepository.save(acc1);

        Accommodation acc2 = new Accommodation();
        acc2.setName("City Hotel");
        acc2.setType("Hotel");
        acc2.setAddress("2 City Center");
        accommodationRepository.save(acc2);

        // Act
        List<Accommodation> result = accommodationRepository.findByNameContainingIgnoreCase("resort");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Beautiful Resort in Bali");
    }
}
