package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role testRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setName("ROLE_USER");
        roleRepository.save(testRole);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(testRole);
        userRepository.save(testUser);
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        Optional<User> result = userRepository.findByUsername("testuser");
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnUser_WhenExists() {
        Optional<User> result = userRepository.findByEmailIgnoreCase("TEST@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByUsernameOrEmailIgnoreCase_ShouldReturnUser_WhenUsernameMatches() {
        Optional<User> result = userRepository.findByUsernameOrEmailIgnoreCase("testuser", "wrong@example.com");
        assertThat(result).isPresent();
    }

    @Test
    void findByUsernameOrEmailIgnoreCase_ShouldReturnUser_WhenEmailMatches() {
        Optional<User> result = userRepository.findByUsernameOrEmailIgnoreCase("wronguser", "TEST@example.com");
        assertThat(result).isPresent();
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenExists() {
        boolean exists = userRepository.existsByUsername("testuser");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailIgnoreCase_ShouldReturnTrue_WhenExists() {
        boolean exists = userRepository.existsByEmailIgnoreCase("TEST@EXAMPLE.COM");
        assertThat(exists).isTrue();
    }

    @Test
    void findByRole_ShouldReturnUsers_WhenRoleMatches() {
        List<User> result = userRepository.findByRole(testRole);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }
}
