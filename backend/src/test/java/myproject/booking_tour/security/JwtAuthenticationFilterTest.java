package myproject.booking_tour.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldSkip_WhenNoAuthHeader() throws Exception {
        when(request.getHeader(SecurityConstants.AUTH_HEADER)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_ShouldSkip_WhenHeaderDoesNotStartWithBearer() throws Exception {
        when(request.getHeader(SecurityConstants.AUTH_HEADER)).thenReturn("InvalidToken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_ShouldAuthenticate_WhenTokenIsValid() throws Exception {
        String token = "valid-token";
        String username = "testuser";

        when(request.getHeader(SecurityConstants.AUTH_HEADER)).thenReturn(SecurityConstants.TOKEN_PREFIX + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        when(jwtService.isTokenValid(token, username)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Access token het han khong con bi filter chan bang 401 tu no. Filter chi
     * de request di tiep ma khong gan danh tinh; endpoint can quyen se roi vao
     * RestAuthenticationEntryPoint va tra 401 - do la tin hieu frontend dung de
     * goi /api/auth/refresh.
     */
    @Test
    void doFilterInternal_ShouldContinueUnauthenticated_WhenTokenIsExpired() throws Exception {
        String token = "expired-token";

        when(request.getHeader(SecurityConstants.AUTH_HEADER)).thenReturn(SecurityConstants.TOKEN_PREFIX + token);
        when(jwtService.extractUsername(token))
                .thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expired"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    /**
     * Filter KHONG con tra bat ky danh sach den nao. Voi access token 15 phut,
     * viec thu hoi som duoc lo boi refresh token; doi lai moi request tren toan
     * he thong bot mot query xuong database.
     */
    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenSignatureIsInvalid() throws Exception {
        String token = "tampered-token";
        String username = "testuser";

        when(request.getHeader(SecurityConstants.AUTH_HEADER)).thenReturn(SecurityConstants.TOKEN_PREFIX + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);
        when(jwtService.isTokenValid(token, username)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
