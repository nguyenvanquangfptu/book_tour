package myproject.booking_tour.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Dia chi IP va trinh duyet cua ben goi, ghi kem moi refresh token.
 *
 * Khong dung de phan quyen - ca hai deu do client tu khai va deu gia mao duoc.
 * Chi phuc vu email canh bao: nguoi dung doc "dang nhap tu 42.115.x.x, Chrome
 * tren Windows" va tu doi chieu xem co phai minh khong.
 */
public record ClientMetadata(String ipAddress, String userAgent) {

    private static final int MAX_USER_AGENT_LENGTH = 255;

    public static ClientMetadata from(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded == null || forwarded.isBlank())
                ? request.getRemoteAddr()
                : forwarded.split(",")[0].trim();

        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > MAX_USER_AGENT_LENGTH) {
            userAgent = userAgent.substring(0, MAX_USER_AGENT_LENGTH);
        }
        return new ClientMetadata(ip, userAgent);
    }
}
