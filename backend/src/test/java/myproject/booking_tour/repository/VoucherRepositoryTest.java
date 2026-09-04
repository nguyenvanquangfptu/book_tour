package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Voucher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VoucherRepositoryTest {

    @Autowired
    private VoucherRepository voucherRepository;

    @Test
    void findByCode_ShouldReturnVoucher_WhenCodeMatches() {
        // Arrange
        Voucher voucher = new Voucher();
        voucher.setCode("DISCOUNT20");
        voucher.setValidFrom(java.time.LocalDateTime.now());
        voucher.setValidUntil(java.time.LocalDateTime.now().plusDays(1));
        voucherRepository.save(voucher);

        // Act
        Optional<Voucher> result = voucherRepository.findByCode("DISCOUNT20");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("DISCOUNT20");
    }

    @Test
    void existsByCode_ShouldReturnTrue_WhenCodeExists() {
        // Arrange
        Voucher voucher = new Voucher();
        voucher.setCode("DISCOUNT50");
        voucher.setValidFrom(java.time.LocalDateTime.now());
        voucher.setValidUntil(java.time.LocalDateTime.now().plusDays(1));
        voucherRepository.save(voucher);

        // Act
        boolean exists = voucherRepository.existsByCode("DISCOUNT50");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_ShouldReturnFalse_WhenCodeDoesNotExist() {
        // Act
        boolean exists = voucherRepository.existsByCode("UNKNOWN");

        // Assert
        assertThat(exists).isFalse();
    }
}
