package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;
import myproject.booking_tour.security.RefreshCookieFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.LoginRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.response.AuthResponse;
import myproject.booking_tour.dto.response.AuthResult;
import myproject.booking_tour.security.ClientMetadata;
import myproject.booking_tour.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final String COOKIE_NAME = "refresh_token";

    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtService jwtService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private RefreshCookieFactory refreshCookieFactory;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Mockito.when(refreshCookieFactory.create(anyString(), any(LocalDateTime.class)))
                .thenAnswer(invocation -> ResponseCookie.from(COOKIE_NAME, invocation.getArgument(0))
                        .httpOnly(true).path("/api/auth").build());
        Mockito.when(refreshCookieFactory.expired())
                .thenReturn(ResponseCookie.from(COOKIE_NAME, "").httpOnly(true).path("/api/auth").maxAge(0).build());
    }

    private static AuthResult sampleResult() {
        return new AuthResult(new AuthResponse(), "refresh-raw", LocalDateTime.now().plusDays(30));
    }

    @Test
    void register_ShouldReturn200() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@test.com");
        // Phai thoa @StrongPassword: >= 8 ky tu, co ca chu va so.
        request.setPassword("Passw0rd123");
        request.setFullName("Test User");

        Mockito.when(authService.register(any(RegisterRequest.class), any(ClientMetadata.class)))
                .thenReturn(sampleResult());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void login_ShouldReturn200_AndSetHttpOnlyRefreshCookie() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        Mockito.when(authService.login(any(LoginRequest.class), any(ClientMetadata.class)))
                .thenReturn(sampleResult());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(cookie().value(COOKIE_NAME, "refresh-raw"))
                // httpOnly la ca ly do cua thiet ke nay: JavaScript khong duoc
                // doc chia khoa 30 ngay.
                .andExpect(cookie().httpOnly(COOKIE_NAME, true))
                // Refresh token khong duoc lot vao body JSON.
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    @Test
    void refresh_ShouldReadTokenFromCookie_NotFromBody() throws Exception {
        Mockito.when(refreshCookieFactory.readFrom(any())).thenReturn("cookie-refresh-token");
        Mockito.when(authService.refresh(any(), any(ClientMetadata.class))).thenReturn(sampleResult());

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie(COOKIE_NAME, "cookie-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().value(COOKIE_NAME, "refresh-raw"));

        Mockito.verify(authService).refresh(Mockito.eq("cookie-refresh-token"), any(ClientMetadata.class));
    }

    @Test
    void logout_ShouldClearCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie(COOKIE_NAME, "cookie-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(COOKIE_NAME, 0));
    }

    @Test
    void forgotPassword_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
