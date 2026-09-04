package myproject.booking_tour.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import myproject.booking_tour.repository.InvalidatedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.PrintWriter;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    /** Gia tri bam gia lap - noi dung khong quan trong, chi can nhat quan. */
    private static final String TOKEN_HASH = "a1b2c3d4e5f60718293a4b5c6d7e8f90112233445566778899aabbccddeeff00";

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

    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;

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
        
        // Danh sach den luu SHA-256 cua token, khong luu token goc
        when(jwtService.hashToken(token)).thenReturn(TOKEN_HASH);
        when(invalidatedTokenRepository.existsById(TOKEN_HASH)).thenReturn(false);
        when(jwtService.isTokenValid(token, username)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorized_WhenTokenIsBlacklisted() throws Exception {
        String token = "blacklisted-token";
        String username = "testuser";

        when(request.getHeader(SecurityConstants.AUTH_HEADER)).thenReturn(SecurityConstants.TOKEN_PREFIX + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        
        when(jwtService.hashToken(token)).thenReturn(TOKEN_HASH);
        when(invalidatedTokenRepository.existsById(TOKEN_HASH)).thenReturn(true);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer, times(1)).write(anyString());
        verifyNoInteractions(filterChain);

        // Phai tra cuu bang HASH chu khong phai token goc - neu mot trong hai
        // ben (logout / filter) quen bam thi danh sach den mat tac dung im lang.
        verify(invalidatedTokenRepository, never()).existsById(token);
    }
}
