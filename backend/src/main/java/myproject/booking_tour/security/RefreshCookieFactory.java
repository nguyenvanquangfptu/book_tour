package myproject.booking_tour.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Dat va doc refresh token trong cookie httpOnly.
 *
 * DAY LA LY DO CHINH CUA VIEC TACH HAI TOKEN. Access token 15 phut nam trong
 * localStorage nen JavaScript doc duoc - mot lo hong XSS lay duoc no, nhung chi
 * dung duoc toi da 15 phut. Refresh token 30 ngay thi khong: cookie httpOnly
 * nam ngoai tam voi cua moi doan script tren trang.
 *
 * Neu de refresh token trong localStorage thi ca thiet ke nay mat gan het y
 * nghia - ke tan cong lay duoc chia khoa 30 ngay thay vi chia khoa 15 phut.
 *
 * Path gioi han o /api/auth: cookie chi duoc gui kem khi goi /refresh va
 * /logout, khong bam theo hang tram request API khac.
 *
 * SameSite: de Lax khi frontend va backend cung site (dev: localhost:5173 goi
 * localhost:8082 - cookie khong phan biet cong nen van la same-site). Khi trien
 * khai that ma hai ben khac domain thi PHAI dat None va Secure=true, keo theo
 * bat buoc HTTPS ca hai dau - xem app.auth.refresh-cookie.* trong
 * application.properties.
 */
@Component
public class RefreshCookieFactory {

    @Value("${app.auth.refresh-cookie.name}")
    private String cookieName;

    @Value("${app.auth.refresh-cookie.path}")
    private String cookiePath;

    @Value("${app.auth.refresh-cookie.secure}")
    private boolean secure;

    @Value("${app.auth.refresh-cookie.same-site}")
    private String sameSite;

    public String getCookieName() {
        return cookieName;
    }

    /** Cookie mang token moi, song den dung thoi diem token het han. */
    public ResponseCookie create(String rawToken, LocalDateTime expiresAt) {
        Duration maxAge = Duration.between(LocalDateTime.now(), expiresAt);
        if (maxAge.isNegative()) {
            maxAge = Duration.ZERO;
        }
        return baseBuilder(rawToken).maxAge(maxAge).build();
    }

    /** Cookie rong het han ngay - trinh duyet xoa ban cu. */
    public ResponseCookie expired() {
        return baseBuilder("").maxAge(0).build();
    }

    public String readFrom(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .orElse(null);
    }

    private ResponseCookie.ResponseCookieBuilder baseBuilder(String value) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(secure)
                .path(cookiePath)
                .sameSite(sameSite);
    }
}
