package myproject.booking_tour.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set properties that are usually injected by @Value
        ReflectionTestUtils.setField(jwtService, "secretKey", "ThisIsAVeryLongSecretKeyForTestingPurposeOnly1234567890!");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtService.generateToken("testuser");
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken("testuser");
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForValidToken() {
        String token = jwtService.generateToken("testuser");
        boolean isValid = jwtService.isTokenValid(token, "testuser");
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForDifferentUsername() {
        String token = jwtService.generateToken("testuser");
        boolean isValid = jwtService.isTokenValid(token, "wronguser");
        assertThat(isValid).isFalse();
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate() {
        String token = jwtService.generateToken("testuser");
        Date expirationDate = jwtService.extractExpiration(token);
        assertThat(expirationDate).isAfter(new Date());
    }
}
