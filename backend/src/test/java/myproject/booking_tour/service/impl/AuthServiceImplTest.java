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

    @Test
    void logout_ShouldRevokeWholeFamily() {
        authService.logout("some-refresh-token");

        verify(refreshTokenService, times(1)).revokeSession("some-refresh-token");
    }
}
