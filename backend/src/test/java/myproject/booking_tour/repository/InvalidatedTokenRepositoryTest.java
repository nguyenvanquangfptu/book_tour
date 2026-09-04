package myproject.booking_tour.repository;

import myproject.booking_tour.entity.InvalidatedToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InvalidatedTokenRepositoryTest {

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Test
    void deleteAllExpiredSince_ShouldDeleteOnlyExpiredTokens() {
        // Arrange
        long nowMillis = System.currentTimeMillis();
        
        InvalidatedToken expiredToken = new InvalidatedToken();
        expiredToken.setId("expired-token");
        expiredToken.setExpiryTime(new Date(nowMillis - 10000)); // Expired 10s ago
        invalidatedTokenRepository.save(expiredToken);

        InvalidatedToken validToken = new InvalidatedToken();
        validToken.setId("valid-token");
        validToken.setExpiryTime(new Date(nowMillis + 10000)); // Expires in 10s
        invalidatedTokenRepository.save(validToken);

        // Act
        invalidatedTokenRepository.deleteAllExpiredSince(new Date(nowMillis));

        // Assert
        List<InvalidatedToken> remainingTokens = invalidatedTokenRepository.findAll();
        assertThat(remainingTokens).hasSize(1);
        assertThat(remainingTokens.get(0).getId()).isEqualTo("valid-token");
    }
}
