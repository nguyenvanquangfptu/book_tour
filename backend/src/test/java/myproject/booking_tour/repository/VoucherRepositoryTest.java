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

    /**
     * Ma cua voucher da xoa phai dung lai duoc. Tren PostgreSQL dieu nay dua vao
     * index mot phan uq_vouchers_code_active (V16); H2 khong co index mot phan
     * nen o day chi kiem phan ung dung: dong da xoa khong chan va khong lan vao.
     */
    @Test
    void code_ShouldBeReusable_AfterTheVoucherIsDeleted() {
        Voucher old = new Voucher();
        old.setCode("TET");
        old.setDiscountPercentage(10.0);
        old.setValidFrom(java.time.LocalDateTime.now().minusYears(1));
        old.setValidUntil(java.time.LocalDateTime.now().minusMonths(11));
        voucherRepository.saveAndFlush(old);
        voucherRepository.delete(old);
        voucherRepository.flush();

        assertThat(voucherRepository.existsByCode("TET")).isFalse();

        Voucher again = new Voucher();
        again.setCode("TET");
        again.setDiscountPercentage(15.0);
        again.setValidFrom(java.time.LocalDateTime.now());
        again.setValidUntil(java.time.LocalDateTime.now().plusMonths(1));
        voucherRepository.saveAndFlush(again);

        assertThat(voucherRepository.findByCode("TET")).get()
                .extracting(Voucher::getId).isEqualTo(again.getId());
    }

    @Test
    void existsByCode_ShouldReturnFalse_WhenCodeDoesNotExist() {
        // Act
        boolean exists = voucherRepository.existsByCode("UNKNOWN");

        // Assert
        assertThat(exists).isFalse();
    }
}
