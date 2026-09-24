package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Utility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UtilityRepositoryTest {

    @Autowired
    private UtilityRepository utilityRepository;

    @Test
    void findByName_ShouldReturnUtility_WhenNameMatches() {
        // Arrange
        Utility utility = new Utility();
        utility.setName("Wi-Fi");
        utilityRepository.save(utility);

        // Act
        Optional<Utility> result = utilityRepository.findByName("Wi-Fi");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Wi-Fi");
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenUtilityExists() {
        // Arrange
        Utility utility = new Utility();
        utility.setName("Pool");
        utilityRepository.save(utility);

        // Act
        boolean exists = utilityRepository.existsByName("Pool");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenUtilityDoesNotExist() {
        // Act
        boolean exists = utilityRepository.existsByName("Spa");

        // Assert
        assertThat(exists).isFalse();
    }
}
