package myproject.booking_tour.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /**
     * Bucket kem thoi diem cham vao lan cuoi, de con biet cai nao da nguoi ma don.
     */
    private record TrackedBucket(Bucket bucket, java.util.concurrent.atomic.AtomicLong lastSeenEpochSecond) {
        void touch() {
            lastSeenEpochSecond.set(Instant.now().getEpochSecond());
        }
    }

    /** Bucket khong duoc cham toi lau hon khoang nay se bi don. */
    private static final Duration IDLE_BEFORE_EVICTION = Duration.ofMinutes(30);

    private final Map<String, TrackedBucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, TrackedBucket> apiBuckets = new ConcurrentHashMap<>();

    /**
     * Dia chi cua cac proxy dat truoc ung dung (nginx, load balancer...), phan
     * cach bang dau phay. De TRONG khi ung dung nhan request truc tiep.
     *
     * Chi khi request den TU mot trong nhung dia chi nay thi header
     * X-Forwarded-For moi duoc tin.
     */
    @Value("${app.security.trusted-proxies:}")
    private String trustedProxiesRaw;

    /** Khoi tao rong: test dung new RateLimitingFilter() khong goi initFilterBean(). */
    private Set<String> trustedProxies = Set.of();

    @Override
    protected void initFilterBean() {
        trustedProxies = Arrays.stream(trustedProxiesRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private Bucket createNewAuthBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createNewApiBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Khoa dinh danh nguoi goi.
     *
     * X-Forwarded-For la header do CLIENT gui, khong phai do ha tang sinh ra.
     * Ban truoc doc thang header do bat cu khi nao no ton tai, nen chi can gui
     * kem mot gia tri ngau nhien moi request la co bucket moi moi lan - gioi han
     * coi nhu khong ton tai. Dang lo nhat la bucket /api/auth/ (10 req/phut):
     * do la lop chong do mat khau duy nhat cua he thong.
     *
     * Header chi duoc tin khi request den tu mot proxy da khai bao. Khong khai
     * bao gi thi luon dung dia chi that cua ket noi - an toan theo mac dinh.
     */
    private String getClientIP(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null || remoteAddr.isBlank()) {
            // null khong lam khoa cua ConcurrentHashMap duoc, va bo qua gioi han
            // thi cang khong - gom tat ca vao mot bucket chung.
            remoteAddr = "unknown";
        }
        if (!trustedProxies.contains(remoteAddr)) {
            return remoteAddr;
        }

        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return remoteAddr;
        }
        String first = xfHeader.split(",")[0].trim();
        return first.isEmpty() ? remoteAddr : first;
    }

    /**
     * Hai map nay truoc day chi co them, khong bao gio bot: moi IP mot entry, ton
     * tai den luc restart. Ghep voi viec khoa co the gia mao thi thanh mot duong
     * lam can bo nho - moi request mot khoa moi.
     */
    @Scheduled(fixedRate = 600000) // 10 phut
    public void evictIdleBuckets() {
        long cutoff = Instant.now().minus(IDLE_BEFORE_EVICTION).getEpochSecond();
        authBuckets.values().removeIf(b -> b.lastSeenEpochSecond().get() < cutoff);
        apiBuckets.values().removeIf(b -> b.lastSeenEpochSecond().get() < cutoff);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        
        // Skip rate limiting for static resources if any
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Webhook cua cong thanh toan khong duoc chan: PayOS retry theo backoff,
        // dinh 429 la mat luon thong bao da thanh toan.
        if (path.startsWith("/api/payment/payos_transfer_handler")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIP = getClientIP(request);
        Map<String, TrackedBucket> buckets = path.startsWith("/api/auth/") ? authBuckets : apiBuckets;
        boolean isAuthPath = buckets == authBuckets;

        TrackedBucket tracked = buckets.computeIfAbsent(clientIP, k -> new TrackedBucket(
                isAuthPath ? createNewAuthBucket() : createNewApiBucket(),
                new java.util.concurrent.atomic.AtomicLong(Instant.now().getEpochSecond())));
        tracked.touch();

        if (tracked.bucket().tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}");
        }
    }
}
