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
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");
    }

    @Test
    void doFilterInternal_ShouldPass_WhenPathIsNotApi() throws Exception {
        when(request.getRequestURI()).thenReturn("/css/style.css");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldPass_WhenUnderLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldBlock_WhenAuthLimitExceeded() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        
        PrintWriter writer = mock(PrintWriter.class);
        lenient().when(response.getWriter()).thenReturn(writer);

        // Limit for auth is 10, so let's hit it 11 times
        for (int i = 0; i < 11; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(10)).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
