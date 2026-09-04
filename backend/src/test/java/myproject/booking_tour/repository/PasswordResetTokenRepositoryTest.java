package myproject.booking_tour.repository;

import myproject.booking_tour.entity.PasswordResetToken;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("ROLE_USER");
        roleRepository.save(role);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(role);
        userRepository.save(testUser);

        testToken = new PasswordResetToken();
        testToken.setToken("reset-token-123");
        testToken.setUser(testUser);
        testToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(testToken);
    }

    @Test
    void findByToken_ShouldReturnToken_WhenExists() {
        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken("reset-token-123");
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("reset-token-123");
        assertThat(result.get().getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void deleteByUser_ShouldRemoveToken_WhenInvoked() {
        passwordResetTokenRepository.deleteByUser(testUser);
        
        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken("reset-token-123");
        assertThat(result).isEmpty();
    }
}
