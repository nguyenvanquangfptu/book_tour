package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.LoginRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.response.AuthResult;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.exception.UnauthorizedException;
import myproject.booking_tour.repository.PasswordResetTokenRepository;
import myproject.booking_tour.repository.RoleRepository;
import myproject.booking_tour.repository.UserRepository;
import myproject.booking_tour.security.ClientMetadata;
import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.service.EmailService;
import myproject.booking_tour.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final ClientMetadata CLIENT = new ClientMetadata("127.0.0.1", "JUnit");

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role();
        mockRole.setName("CUSTOMER");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("encoded");
        mockUser.setRole(mockRole);
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request, CLIENT));
    }

    @Test
    void login_ShouldReturnAccessAndRefreshToken_WhenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        when(userRepository.findByUsernameOrEmailIgnoreCase("testuser", "testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtService.generateToken("testuser")).thenReturn("jwt.token.here");
        when(refreshTokenService.startSession(eq(mockUser), any(ClientMetadata.class)))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-raw-value", expiresAt));

        AuthResult result = authService.login(request, CLIENT);

        assertNotNull(result);
        assertEquals("jwt.token.here", result.body().getToken());
        // Refresh token di rieng, KHONG duoc nam trong body JSON - controller
        // dat no vao cookie httpOnly.
        assertEquals("refresh-raw-value", result.refreshToken());
        assertEquals(expiresAt, result.refreshExpiresAt());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsernameOrEmailIgnoreCase("testuser", "testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "encoded")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request, CLIENT));
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void refresh_ShouldIssueNewPair_WhenTokenIsValid() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(29);
        when(refreshTokenService.rotate(eq("old-refresh"), any(ClientMetadata.class)))
                .thenReturn(new RefreshTokenService.Rotation(
                        mockUser, new RefreshTokenService.IssuedToken("new-refresh", expiresAt)));
        when(jwtService.generateToken("testuser")).thenReturn("new.jwt.token");

        AuthResult result = authService.refresh("old-refresh", CLIENT);

        assertEquals("new.jwt.token", result.body().getToken());
        // Token cu KHONG duoc tra lai - moi lan lam moi phai sinh gia tri khac.
        assertEquals("new-refresh", result.refreshToken());
    }

    @Test
    void refresh_ShouldThrow_WhenNoCookiePresent() {
        assertThrows(UnauthorizedException.class, () -> authService.refresh(null, CLIENT));
        verifyNoInteractions(refreshTokenService);
    }

    private static com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload googlePayload(
            String email, Boolean emailVerified) {
        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload =
                new com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload();
        payload.setEmail(email);
        payload.setEmailVerified(emailVerified);
        return payload;
    }

    /**
     * Tai khoan Google dang ky duoc bang mot dia chi bat ky ma khong can chung
     * minh la chu hop thu. Token cua no van hop le, chi mang email_verified=false.
     * Coi email do la danh tinh thi ai biet email cua admin cung vao duoc tai
     * khoan admin.
     */
    @Test
    void loginWithGoogle_ShouldRefuse_WhenGoogleHasNotVerifiedTheEmail() {
        AuthServiceImpl service = spy(authService);
        doReturn(googlePayload("test@test.com", false)).when(service).verifyGoogleToken("id-token");

        assertThrows(UnauthorizedException.class, () -> service.loginWithGoogle("id-token", CLIENT));

        verify(userRepository, never()).findByEmailIgnoreCase(any());
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void loginWithGoogle_ShouldRefuse_WhenTokenCarriesNoVerificationClaim() {
        AuthServiceImpl service = spy(authService);
        doReturn(googlePayload("test@test.com", null)).when(service).verifyGoogleToken("id-token");

        assertThrows(UnauthorizedException.class, () -> service.loginWithGoogle("id-token", CLIENT));

        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void loginWithGoogle_ShouldSignInExistingAccount_WhenEmailIsVerified() {
        AuthServiceImpl service = spy(authService);
        doReturn(googlePayload("test@test.com", true)).when(service).verifyGoogleToken("id-token");
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken("testuser")).thenReturn("jwt.token.here");
        when(refreshTokenService.startSession(eq(mockUser), any(ClientMetadata.class)))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-raw-value", LocalDateTime.now().plusDays(30)));

        AuthResult result = service.loginWithGoogle("id-token", CLIENT);

        assertEquals(1L, result.body().getUserId());
        verify(userRepository, never()).save(any());
    }

    /**
     * Su co sau buoc xac minh la cua may chu, khong phai "token Google sai".
     * Truoc day moi ngoai le deu bi boc thanh 401 kem nguyen van thong bao loi.
     */
    @Test
    void loginWithGoogle_ShouldNotDisguiseServerFailuresAsUnauthorized() {
        AuthServiceImpl service = spy(authService);
        doReturn(googlePayload("test@test.com", true)).when(service).verifyGoogleToken("id-token");
        when(userRepository.findByEmailIgnoreCase("test@test.com"))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("connection refused"));

        assertThrows(org.springframework.dao.DataAccessResourceFailureException.class,
                () -> service.loginWithGoogle("id-token", CLIENT));
    }

    @Test
    void logout_ShouldRevokeWholeFamily() {
        authService.logout("some-refresh-token");

        verify(refreshTokenService, times(1)).revokeSession("some-refresh-token");
    }

    /**
     * Email khong ton tai phai ket thuc IM LANG, khong duoc nem ngoai le.
     *
     * Truoc day cho nay la .orElseThrow(ResourceNotFoundException) va tra ve 404
     * kem nguyen van "User not found with email: x@y.com" - bien endpoint nay
     * thanh cong cu do danh sach tai khoan da dang ky.
     */
    @Test
    void forgotPassword_ShouldStaySilent_WhenEmailDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("khongtontai@example.com"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.forgotPassword("khongtontai@example.com"));

        // Khong tao ma, khong gui mail - ben ngoai khong the phan biet duoc
        // truong hop nay voi truong hop email co that.
        verifyNoInteractions(tokenRepository, emailService);
    }

    /**
     * Ma OTP goc chi duoc di vao email. Database chi nhan ban bam SHA-256.
     */
    @Test
    void forgotPassword_ShouldStoreOnlyTheHash_AndEmailTheRawCode() throws Exception {
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(mockUser));

        authService.forgotPassword("test@test.com");

        ArgumentCaptor<myproject.booking_tour.entity.PasswordResetToken> saved =
                ArgumentCaptor.forClass(myproject.booking_tour.entity.PasswordResetToken.class);
        verify(tokenRepository).save(saved.capture());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Map<String, Object>> model = ArgumentCaptor.forClass(java.util.Map.class);
        verify(emailService).sendMessageUsingThymeleafTemplate(
                eq("test@test.com"), anyString(), eq("reset-password"), model.capture());

        String rawOtp = (String) model.getValue().get("resetToken");
        assertEquals(6, rawOtp.length(), "OTP phai la 6 chu so");

        String storedHash = saved.getValue().getTokenHash();
        assertEquals(64, storedHash.length(), "SHA-256 hex luon 64 ky tu");
        assertNotEquals(rawOtp, storedHash, "Ma goc KHONG duoc nam trong database");
        assertEquals(myproject.booking_tour.security.TokenHasher.sha256Hex(rawOtp), storedHash);
    }

    private myproject.booking_tour.entity.PasswordResetToken liveCodeFor(User user, String rawCode) {
        return new myproject.booking_tour.entity.PasswordResetToken(
                myproject.booking_tour.security.TokenHasher.sha256Hex(rawCode), user, LocalDateTime.now().plusMinutes(5));
    }

    @Test
    void resetPassword_ShouldChangePasswordAndEndSessions_WhenCodeMatchesTheAccount() {
        myproject.booking_tour.entity.PasswordResetToken code = liveCodeFor(mockUser, "123456");
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(mockUser));
        when(tokenRepository.findFirstByUserOrderByIdDesc(mockUser)).thenReturn(Optional.of(code));
        when(passwordEncoder.encode("Matkhau123")).thenReturn("new-hash");

        authService.resetPassword("test@test.com", "123456", "Matkhau123");

        assertEquals("new-hash", mockUser.getPassword());
        verify(tokenRepository).delete(code);
        verify(refreshTokenService).revokeAllSessions(eq(1L), any());
    }

    /**
     * Truoc day ma duoc tra tren TOAN BANG: doan trung ma 6 so cua bat ky ai la
     * doi duoc mat khau cua nguoi do. Gio ma phai thuoc dung tai khoan mang
     * email duoc gui len.
     */
    @Test
    void resetPassword_ShouldRefuse_WhenTheCodeBelongsToAnotherAccount() {
        // "222222" la ma cua mot tai khoan khac; gui kem email nay thi vo nghia.
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(mockUser));
        when(tokenRepository.findFirstByUserOrderByIdDesc(mockUser)).thenReturn(Optional.of(liveCodeFor(mockUser, "111111")));

        assertThrows(BadRequestException.class,
                () -> authService.resetPassword("test@test.com", "222222", "Matkhau123"));

        verify(passwordEncoder, never()).encode(any());
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void resetPassword_ShouldCountWrongGuesses_AndDestroyTheCodeAtTheLimit() {
        myproject.booking_tour.entity.PasswordResetToken code = liveCodeFor(mockUser, "123456");
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(mockUser));
        when(tokenRepository.findFirstByUserOrderByIdDesc(mockUser)).thenReturn(Optional.of(code));

        for (int i = 1; i < AuthServiceImpl.MAX_RESET_CODE_ATTEMPTS; i++) {
            assertThrows(BadRequestException.class,
                    () -> authService.resetPassword("test@test.com", "000000", "Matkhau123"));
            assertEquals(i, code.getFailedAttempts());
        }
        verify(tokenRepository, never()).delete(any());

        assertThrows(BadRequestException.class,
                () -> authService.resetPassword("test@test.com", "000000", "Matkhau123"));
        verify(tokenRepository).delete(code);

        verify(passwordEncoder, never()).encode(any());
    }

    /** Email khong ton tai va ma sai phai tra ve cung mot cau. */
    @Test
    void resetPassword_ShouldNotRevealWhetherTheEmailExists() {
        when(userRepository.findByEmailIgnoreCase("khongtontai@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(mockUser));
        when(tokenRepository.findFirstByUserOrderByIdDesc(mockUser)).thenReturn(Optional.of(liveCodeFor(mockUser, "123456")));

        BadRequestException unknown = assertThrows(BadRequestException.class,
                () -> authService.resetPassword("khongtontai@example.com", "123456", "Matkhau123"));
        BadRequestException wrong = assertThrows(BadRequestException.class,
                () -> authService.resetPassword("test@test.com", "654321", "Matkhau123"));

        assertEquals(unknown.getMessage(), wrong.getMessage());
    }

    @Test
    void resetPassword_ShouldNotCountAgainstTheLimit_WhenTheTransactionRollsBack() throws Exception {
        // So lan sai chi co gia tri neu no duoc ghi xuong. BadRequestException la
        // RuntimeException - mac dinh Spring quay lui giao dich va bo dem mat.
        org.springframework.transaction.annotation.Transactional tx = AuthServiceImpl.class
                .getMethod("resetPassword", String.class, String.class, String.class)
                .getAnnotation(org.springframework.transaction.annotation.Transactional.class);

        assertNotNull(tx);
        assertTrue(java.util.Arrays.asList(tx.noRollbackFor()).contains(BadRequestException.class));
    }

    /** Dang bi khoa cung phai im lang - khong duoc de lo rang email nay co that. */
    @Test
    void forgotPassword_ShouldStaySilent_WhenUserIsBanned() {
        mockUser.setForgotPasswordBanUntil(LocalDateTime.now().plusHours(2));
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(mockUser));

        assertDoesNotThrow(() -> authService.forgotPassword("test@test.com"));

        verifyNoInteractions(tokenRepository, emailService);
    }
}
