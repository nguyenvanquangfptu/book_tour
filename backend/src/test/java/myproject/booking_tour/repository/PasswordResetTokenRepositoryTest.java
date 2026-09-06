package myproject.booking_tour.repository;

import myproject.booking_tour.entity.PasswordResetToken;
import myproject.booking_tour.security.TokenHasher;
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
        testToken.setTokenHash(TokenHasher.sha256Hex("123456"));
        testToken.setUser(testUser);
        testToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(testToken);
    }

    /**
     * Truoc day khong ai don bang nay: deleteByUser() chi chay khi CHINH nguoi
     * do xin ma moi, nen ma cua nhung nguoi khong quay lai nam do mai mai.
     */
    @Test
    void deleteExpired_ShouldRemoveOnlyExpiredCodes() {
        PasswordResetToken expired = new PasswordResetToken();
        expired.setTokenHash(TokenHasher.sha256Hex("999999"));
        expired.setUser(testUser);
        expired.setExpiryDate(LocalDateTime.now().minusMinutes(10));
        passwordResetTokenRepository.save(expired);

        int deleted = passwordResetTokenRepository.deleteExpired(LocalDateTime.now());

        assertThat(deleted).isEqualTo(1);
        // Ma con han khong duoc dung toi
        assertThat(passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256Hex("123456")))
                .isPresent();
        assertThat(passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256Hex("999999")))
                .isEmpty();
    }

    @Test
    void findByTokenHash_ShouldReturnToken_WhenExists() {
        Optional<PasswordResetToken> result =
                passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256Hex("123456"));
        assertThat(result).isPresent();
        assertThat(result.get().getTokenHash()).isEqualTo(TokenHasher.sha256Hex("123456"));
        // Ban goc KHONG duoc nam trong database
        assertThat(result.get().getTokenHash()).isNotEqualTo("123456");
        assertThat(result.get().getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void deleteByUser_ShouldRemoveToken_WhenInvoked() {
        passwordResetTokenRepository.deleteByUser(testUser);
        
        Optional<PasswordResetToken> result =
                passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256Hex("123456"));
        assertThat(result).isEmpty();
    }
}
