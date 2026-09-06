package myproject.booking_tour.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Tra 401 khi request khong mang danh tinh hop le.
 *
 * KHONG PHAI CHI CHO DEP. Mac dinh cua Spring Security trong cau hinh nay la
 * tra 403 cho ca truong hop "chua dang nhap", va frontend phan biet hai ma nay
 * theo dung nghia cua chung:
 *     401 = danh tinh khong con hop le -> goi /api/auth/refresh
 *     403 = da dang nhap nhung khong du quyen -> hien thong bao, khong dang xuat
 * Neu thieu entry point nay, access token het han sau 15 phut se tra 403 va
 * frontend khong bao gio kich hoat viec lam moi - nguoi dung bi day ra ngoai
 * moi 15 phut.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"Phiên đăng nhập không hợp lệ hoặc đã hết hạn.\",\"data\":null}");
    }
}
