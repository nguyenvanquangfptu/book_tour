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
import org.springframework.test.util.ReflectionTestUtils;

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
        lenient().when(request.getRemoteAddr()).thenReturn("203.0.113.9");
    }

    /** Không khai báo proxy nào — mặc định, và là cấu hình an toàn. */
    private void withoutTrustedProxies() {
        ReflectionTestUtils.setField(rateLimitingFilter, "trustedProxiesRaw", "");
        ReflectionTestUtils.invokeMethod(rateLimitingFilter, "initFilterBean");
    }

    private void withTrustedProxy(String proxyAddress) {
        ReflectionTestUtils.setField(rateLimitingFilter, "trustedProxiesRaw", proxyAddress);
        ReflectionTestUtils.invokeMethod(rateLimitingFilter, "initFilterBean");
    }

    @Test
    void doFilterInternal_ShouldPass_WhenPathIsNotApi() throws Exception {
        when(request.getRequestURI()).thenReturn("/css/style.css");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldPass_WhenUnderLimit() throws Exception {
        withoutTrustedProxies();
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldBlock_WhenAuthLimitExceeded() throws Exception {
        withoutTrustedProxies();
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

    /**
     * Đây là lỗ hổng cũ: X-Forwarded-For là header CLIENT tự gửi, nên chỉ cần đổi
     * giá trị mỗi request là có bucket mới mỗi lần — giới hạn 10 lần đăng
     * nhập/phút coi như không tồn tại, và đó là lớp chống dò mật khẩu duy nhất.
     */
    @Test
    void doFilterInternal_ShouldIgnoreForwardedHeader_WhenPeerIsNotATrustedProxy() throws Exception {
        withoutTrustedProxies();
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        // Mỗi lần gọi trả về một IP giả khác nhau, đúng như kẻ tấn công sẽ làm.
        // lenient() vì bản đã sửa không thèm đọc header này nữa - và đó chính là
        // điều test khẳng định ở cuối.
        lenient().when(request.getHeader("X-Forwarded-For"))
                .thenReturn("1.1.1.1", "2.2.2.2", "3.3.3.3", "4.4.4.4", "5.5.5.5",
                            "6.6.6.6", "7.7.7.7", "8.8.8.8", "9.9.9.9", "10.10.10.10", "11.11.11.11");

        PrintWriter writer = mock(PrintWriter.class);
        lenient().when(response.getWriter()).thenReturn(writer);

        for (int i = 0; i < 11; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Vẫn đúng 10 lần lọt: tất cả dùng chung một bucket theo địa chỉ thật.
        verify(filterChain, times(10)).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        // Không proxy nào được khai báo thì header thậm chí không được đọc tới.
        verify(request, never()).getHeader("X-Forwarded-For");
    }

    @Test
    void doFilterInternal_ShouldHonourForwardedHeader_WhenPeerIsATrustedProxy() throws Exception {
        // Sau nginx thì mọi request đều đến từ địa chỉ của nginx; nếu bỏ qua
        // header thì cả hệ thống dùng chung một bucket và một người đăng nhập
        // sai vài lần sẽ khoá tất cả.
        withTrustedProxy("203.0.113.9");
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.1", "198.51.100.2");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Hai khách khác nhau, hai bucket riêng, không ai bị chặn.
        verify(filterChain, times(2)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void doFilterInternal_ShouldNotCrash_WhenRemoteAddressIsMissing() throws Exception {
        withoutTrustedProxies();
        when(request.getRequestURI()).thenReturn("/api/tours/search");
        when(request.getRemoteAddr()).thenReturn(null);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // null không làm khoá của ConcurrentHashMap được - phải có giá trị thay thế.
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void evictIdleBuckets_ShouldKeepBucketsStillInUse() throws Exception {
        // Dọn định kỳ không được xoá bucket đang dùng, nếu không giới hạn tự
        // reset sau mỗi lần quét và cũng thành vô dụng.
        withoutTrustedProxies();
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        PrintWriter writer = mock(PrintWriter.class);
        lenient().when(response.getWriter()).thenReturn(writer);

        for (int i = 0; i < 10; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitingFilter.evictIdleBuckets();

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(10)).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
