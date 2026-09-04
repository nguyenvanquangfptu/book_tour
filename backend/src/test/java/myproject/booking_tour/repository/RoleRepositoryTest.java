package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_ShouldReturnRole_WhenNameMatches() {
        // Arrange
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        roleRepository.save(role);

        // Act
        Optional<Role> result = roleRepository.findByName("ROLE_ADMIN");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenRoleExists() {
        // Arrange
        Role role = new Role();
        role.setName("ROLE_USER");
        roleRepository.save(role);

        // Act
        boolean exists = roleRepository.existsByName("ROLE_USER");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenRoleDoesNotExist() {
        // Act
        boolean exists = roleRepository.existsByName("ROLE_UNKNOWN");

        // Assert
        assertThat(exists).isFalse();
    }
}
