package myproject.booking_tour.repository;

import myproject.booking_tour.entity.RefreshToken;
import myproject.booking_tour.entity.RefreshToken.RevocationReason;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test tich hop cho cac lenh bulk update cua RefreshTokenRepository.
 *
 * Phai la @DataJpaTest chu khong phai Mockito: thu duoc kiem o day - tuong tac
 * giua mot lenh bulk JPQL va cac thay doi dang cho trong persistence context -
 * khong ton tai trong the gioi mock.
 */
@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private UUID familyId;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("CUSTOMER");
        entityManager.persist(role);

        user = new User();
        user.setUsername("testuser");
        user.setFullName("Ten Ban Dau");
        user.setEmail("test@example.com");
        user.setPassword("mat-khau-cu");
        user.setRole(role);
        entityManager.persist(user);

        familyId = UUID.randomUUID();
        RefreshToken token = new RefreshToken();
        token.setTokenHash("a".repeat(64));
        token.setUser(user);
        token.setFamilyId(familyId);
        token.setIssuedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(30));
        entityManager.persist(token);

        entityManager.flush();
        entityManager.clear();
    }

    /**
     * LOI DA TUNG XAY RA THAT: doi mat khau bao thanh cong nhung mat khau khong doi.
     *
     * resetPassword() dat mat khau moi len thuc the User dang duoc quan ly, roi
     * goi revokeAllForUser(). Neu lenh bulk do chi co clearAutomatically ma
     * thieu flushAutomatically thi no chay thang xuong database bo qua thay doi
     * dang cho, sau do xoa sach persistence context - mat khau moi bien mat ma
     * khong mot loi nao duoc nem ra.
     *
     * Test nay dung mot truong bat ky de mo phong: thay doi chua flush phai
     * SONG SOT qua lenh bulk.
     */
    @Test
    void revokeAllForUser_ShouldNotDiscardPendingChanges() {
        User managed = entityManager.find(User.class, user.getId());
        managed.setFullName("Ten Da Doi");   // dirty, chua flush

        refreshTokenRepository.revokeAllForUser(
                user.getId(), LocalDateTime.now(), RevocationReason.PASSWORD_RESET);

        // Context da bi clear, nen find() doc lai tu database.
        User reloaded = entityManager.find(User.class, user.getId());
        assertThat(reloaded.getFullName())
                .as("Thay doi dang cho phai duoc flush TRUOC lenh bulk, khong duoc bi clear cuon di")
                .isEqualTo("Ten Da Doi");
    }

    @Test
    void revokeFamily_ShouldNotDiscardPendingChanges() {
        User managed = entityManager.find(User.class, user.getId());
        managed.setFullName("Ten Da Doi");

        refreshTokenRepository.revokeFamily(
                familyId, LocalDateTime.now(), RevocationReason.REUSE_DETECTED);

        User reloaded = entityManager.find(User.class, user.getId());
        assertThat(reloaded.getFullName()).isEqualTo("Ten Da Doi");
    }

    @Test
    void revokeAllForUser_ShouldRevokeEveryLiveToken() {
        int revoked = refreshTokenRepository.revokeAllForUser(
                user.getId(), LocalDateTime.now(), RevocationReason.PASSWORD_RESET);

        assertThat(revoked).isEqualTo(1);

        RefreshToken reloaded = refreshTokenRepository.findAll().get(0);
        assertThat(reloaded.getRevokedAt()).isNotNull();
        assertThat(reloaded.getRevokedReason()).isEqualTo(RevocationReason.PASSWORD_RESET);
    }

    /** Token da bi thu hoi thi giu nguyen ly do cu, khong bi ghi de. */
    @Test
    void revokeFamily_ShouldSkipAlreadyRevokedTokens() {
        refreshTokenRepository.revokeFamily(familyId, LocalDateTime.now(), RevocationReason.LOGOUT);

        int second = refreshTokenRepository.revokeFamily(
                familyId, LocalDateTime.now(), RevocationReason.REUSE_DETECTED);

        assertThat(second).isZero();
        assertThat(refreshTokenRepository.findAll().get(0).getRevokedReason())
                .isEqualTo(RevocationReason.LOGOUT);
    }
}
