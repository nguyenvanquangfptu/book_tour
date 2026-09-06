package myproject.booking_tour.service;

import myproject.booking_tour.entity.RefreshToken;
import myproject.booking_tour.entity.RefreshToken.RevocationReason;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.TokenReuseException;
import myproject.booking_tour.exception.UnauthorizedException;
import myproject.booking_tour.repository.RefreshTokenRepository;
import myproject.booking_tour.security.ClientMetadata;
import myproject.booking_tour.security.TokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Trai tim cua co che: mot lan dung, xoay vong theo family, va phat hien khi
 * token da tieu quay tro lai.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final long THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000;
    private static final ClientMetadata CLIENT = new ClientMetadata("203.0.113.9", "JUnit/1.0");

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", THIRTY_DAYS_MS);

        user = new User();
        user.setId(7L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setFullName("Test User");
    }

    private RefreshToken activeToken(String rawValue, UUID familyId, LocalDateTime expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setId(100L);
        token.setTokenHash(TokenHasher.sha256Hex(rawValue));
        token.setUser(user);
        token.setFamilyId(familyId);
        token.setIssuedAt(LocalDateTime.now().minusMinutes(20));
        token.setExpiresAt(expiresAt);
        return token;
    }

    private RefreshToken captureSaved() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void startSession_ShouldCreateNewFamily_ExpiringIn30Days() {
        RefreshTokenService.IssuedToken issued = refreshTokenService.startSession(user, CLIENT);
        RefreshToken saved = captureSaved();

        assertNotNull(saved.getFamilyId());
        assertNull(saved.getParentId(), "Token dau tien cua mot phien khong co cha");
        assertNull(saved.getUsedAt());
        assertNull(saved.getRevokedAt());

        // Ban goc khong duoc luu vao database - chi ban bam.
        assertEquals(TokenHasher.sha256Hex(issued.rawValue()), saved.getTokenHash());
        assertNotEquals(issued.rawValue(), saved.getTokenHash());

        // Han khoang 30 ngay. Khong so sanh dung bang: giua luc cap va luc kiem
        // tra co mot khoang thoi gian nho khong xac dinh.
        LocalDateTime now = LocalDateTime.now();
        assertTrue(issued.expiresAt().isAfter(now.plusDays(29).plusHours(23)));
        assertFalse(issued.expiresAt().isAfter(now.plusDays(30)));
    }

    @Test
    void rotate_ShouldSpendOldToken_AndIssueSuccessorInSameFamily() {
        UUID familyId = UUID.randomUUID();
        LocalDateTime familyExpiry = LocalDateTime.now().plusDays(25);
        RefreshToken current = activeToken("raw-token", familyId, familyExpiry);
        when(refreshTokenRepository.lockByTokenHash(TokenHasher.sha256Hex("raw-token")))
                .thenReturn(Optional.of(current));

        RefreshTokenService.Rotation rotation = refreshTokenService.rotate("raw-token", CLIENT);

        // Token cu chet ngay tai day - day la toan bo y nghia cua "mot lan dung".
        assertNotNull(current.getUsedAt());
        assertNotNull(current.getRevokedAt());
        assertEquals(RevocationReason.ROTATED, current.getRevokedReason());
        assertTrue(current.isUsed());

        RefreshToken successor = captureSaved();
        assertEquals(familyId, successor.getFamilyId(), "Ke thua cung mot phien");
        assertEquals(current.getId(), successor.getParentId(), "Giu duoc chuoi truy vet");
        assertNull(successor.getUsedAt());

        // Xoay vong KHONG gia han: han van la han cua family.
        assertEquals(familyExpiry, successor.getExpiresAt());
        assertEquals(familyExpiry, rotation.token().expiresAt());

        assertNotEquals("raw-token", rotation.token().rawValue());
        assertEquals(user, rotation.user());
        verify(refreshTokenRepository, never()).revokeFamily(any(), any(), any());
    }

    @Test
    void rotate_ShouldRevokeWholeFamilyAndAlert_WhenTokenAlreadyUsed() throws Exception {
        UUID familyId = UUID.randomUUID();
        RefreshToken spent = activeToken("stolen-token", familyId, LocalDateTime.now().plusDays(25));
        spent.setUsedAt(LocalDateTime.now().minusMinutes(5));
        spent.setRevokedAt(LocalDateTime.now().minusMinutes(5));
        spent.setRevokedReason(RevocationReason.ROTATED);

        when(refreshTokenRepository.lockByTokenHash(TokenHasher.sha256Hex("stolen-token")))
                .thenReturn(Optional.of(spent));

        assertThrows(TokenReuseException.class, () -> refreshTokenService.rotate("stolen-token", CLIENT));

        // Ca family bi thu hoi, khong phai chi rieng token nay: khong biet ai la
        // ke trom nen ca hai ben deu phai dang nhap lai.
        verify(refreshTokenRepository).revokeFamily(eq(familyId), any(LocalDateTime.class),
                eq(RevocationReason.REUSE_DETECTED));
        // Khong duoc cap token moi cho ke goi.
        verify(refreshTokenRepository, never()).save(any());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> model = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendMessageUsingThymeleafTemplate(
                eq("test@test.com"), anyString(), eq("security-alert"), model.capture());
        assertEquals("203.0.113.9", model.getValue().get("ipAddress"));
    }

    /**
     * Token bi thu hoi nhung CHUA TUNG duoc dung: nguyen nhan luon la do he
     * thong (logout, doi mat khau, day chuyen thu hoi cua mot vu da canh bao).
     * Khong co ai tan cong ca - tu choi im lang, tuyet doi khong gui email.
     */
    @Test
    void rotate_ShouldRejectQuietly_WhenTokenRevokedButNeverUsed() {
        UUID familyId = UUID.randomUUID();
        RefreshToken revoked = activeToken("logged-out-token", familyId, LocalDateTime.now().plusDays(25));
        revoked.setRevokedAt(LocalDateTime.now().minusMinutes(1));
        revoked.setRevokedReason(RevocationReason.LOGOUT);

        when(refreshTokenRepository.lockByTokenHash(TokenHasher.sha256Hex("logged-out-token")))
                .thenReturn(Optional.of(revoked));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> refreshTokenService.rotate("logged-out-token", CLIENT));
        assertFalse(ex instanceof TokenReuseException);

        verify(refreshTokenRepository, never()).revokeFamily(any(), any(), any());
        verifyNoInteractions(emailService);
    }

    /**
     * Het han duoc kiem TRUOC co "da dung". Mot thiet bi offline qua 30 ngay bat
     * len va thu lai token cu khong phai la tan cong - ca family da chet tu lau.
     */
    @Test
    void rotate_ShouldRejectQuietly_WhenTokenUsedButAlreadyExpired() {
        RefreshToken expired = activeToken("stale-token", UUID.randomUUID(), LocalDateTime.now().minusDays(1));
        expired.setUsedAt(LocalDateTime.now().minusDays(31));
        expired.setRevokedAt(LocalDateTime.now().minusDays(31));
        expired.setRevokedReason(RevocationReason.ROTATED);

        when(refreshTokenRepository.lockByTokenHash(TokenHasher.sha256Hex("stale-token")))
                .thenReturn(Optional.of(expired));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> refreshTokenService.rotate("stale-token", CLIENT));
        assertFalse(ex instanceof TokenReuseException);

        verify(refreshTokenRepository, never()).revokeFamily(any(), any(), any());
        verifyNoInteractions(emailService);
    }

    /**
     * Mot vu xam nhap = MOT email. Ke tan cong thuong thu lai nhieu lan sau khi
     * bi cat quyen; moi lan deu la phat hien dung lai hop le va van phai vao
     * log, nhung khong duoc bien thanh mot chuoi email trung nhau.
     */
    @Test
    void rotate_ShouldNotEmailTwice_ForTheSameIncident() throws Exception {
        UUID familyId = UUID.randomUUID();
        RefreshToken spent = activeToken("retry-token", familyId, LocalDateTime.now().plusDays(25));
        spent.setUsedAt(LocalDateTime.now().minusMinutes(5));
        spent.setRevokedAt(LocalDateTime.now().minusMinutes(5));
        spent.setRevokedReason(RevocationReason.ROTATED);

        when(refreshTokenRepository.lockByTokenHash(TokenHasher.sha256Hex("retry-token")))
                .thenReturn(Optional.of(spent));
        // Family nay da bi gan co tu lan phat hien truoc.
        when(refreshTokenRepository.existsByFamilyIdAndRevokedReason(familyId, RevocationReason.REUSE_DETECTED))
                .thenReturn(true);

        assertThrows(TokenReuseException.class, () -> refreshTokenService.rotate("retry-token", CLIENT));

        // Van thu hoi (vo hai, family da chet) va van tu choi...
        verify(refreshTokenRepository).revokeFamily(eq(familyId), any(LocalDateTime.class),
                eq(RevocationReason.REUSE_DETECTED));
        // ...nhung khong lam phien nguoi dung them lan nua.
        verify(emailService, never()).sendMessageUsingThymeleafTemplate(
                anyString(), anyString(), anyString(), any());
    }

    /**
     * Het han la chuyen binh thuong sau 30 ngay - KHONG duoc coi la tan cong,
     * khong thu hoi gi ca, khong lam nguoi dung hoang so bang email canh bao.
     */
    @Test
    void rotate_ShouldRejectQuietly_WhenTokenExpired() {
        RefreshToken expired = activeToken("old-token", UUID.randomUUID(), LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.lockByTokenHash(TokenHasher.sha256Hex("old-token")))
                .thenReturn(Optional.of(expired));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> refreshTokenService.rotate("old-token", CLIENT));
        assertFalse(ex instanceof TokenReuseException);

        verify(refreshTokenRepository, never()).revokeFamily(any(), any(), any());
        verifyNoInteractions(emailService);
    }

    @Test
    void rotate_ShouldReject_WhenTokenUnknown() {
        when(refreshTokenRepository.lockByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.rotate("never-issued", CLIENT));
        verifyNoInteractions(emailService);
    }

    @Test
    void revokeSession_ShouldRevokeWholeFamily() {
        UUID familyId = UUID.randomUUID();
        RefreshToken current = activeToken("logout-token", familyId, LocalDateTime.now().plusDays(10));
        when(refreshTokenRepository.lockByTokenHash(TokenHasher.sha256Hex("logout-token")))
                .thenReturn(Optional.of(current));

        refreshTokenService.revokeSession("logout-token");

        verify(refreshTokenRepository).revokeFamily(eq(familyId), any(LocalDateTime.class),
                eq(RevocationReason.LOGOUT));
    }

    @Test
    void revokeSession_ShouldDoNothing_WhenNoTokenGiven() {
        refreshTokenService.revokeSession(null);
        refreshTokenService.revokeSession("   ");

        verifyNoInteractions(refreshTokenRepository);
    }

    /** Hai lan cap phai ra hai gia tri khac nhau - neu trung thi khoa duy nhat vo nghia. */
    @Test
    void issuedTokens_ShouldNeverRepeat() {
        String first = refreshTokenService.startSession(user, CLIENT).rawValue();
        String second = refreshTokenService.startSession(user, CLIENT).rawValue();

        assertNotEquals(first, second);
        assertEquals(43, first.length(), "32 byte ma hoa Base64 URL khong dem");
    }
}
