package myproject.booking_tour.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.LoginRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.request.ResetPasswordRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.AuthResponse;
import myproject.booking_tour.dto.response.AuthResult;
import myproject.booking_tour.security.ClientMetadata;
import myproject.booking_tour.security.RefreshCookieFactory;
import myproject.booking_tour.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request,
                                                              HttpServletRequest httpRequest) {
        AuthResult result = authService.register(request, ClientMetadata.from(httpRequest));
        return withRefreshCookie(result, "User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest httpRequest) {
        AuthResult result = authService.login(request, ClientMetadata.from(httpRequest));
        return withRefreshCookie(result, "User logged in successfully!");
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(@RequestBody Map<String, String> body,
                                                                     HttpServletRequest httpRequest) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "idToken is required!", null));
        }
        AuthResult result = authService.loginWithGoogle(idToken, ClientMetadata.from(httpRequest));
        return withRefreshCookie(result, "Google login successful!");
    }

    /**
     * Doi refresh token trong cookie lay access token moi.
     *
     * Endpoint duy nhat (cung /logout) ma trinh duyet gui kem cookie, vi cookie
     * duoc dat path=/api/auth. Khong nhan token tu body hay header: neu chap
     * nhan ca hai duong thi ke tan cong co XSS chi can goi thang endpoint nay
     * la lai co token moi - dung viec dung cookie httpOnly thanh vo nghia.
     *
     * Refresh token moi di ra bang Set-Cookie, KHONG nam trong JSON tra ve.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(HttpServletRequest httpRequest) {
        String refreshToken = refreshCookieFactory.readFrom(httpRequest);
        AuthResult result = authService.refresh(refreshToken, ClientMetadata.from(httpRequest));
        return withRefreshCookie(result, "Token refreshed successfully!");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        authService.logout(refreshCookieFactory.readFrom(httpRequest));
        // Xoa cookie du phia server co tim thay token hay khong: trinh duyet
        // khong duoc phep giu lai mot chuoi da vo nghia.
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.expired().toString())
                .body(new ApiResponse<>(true, "Logged out successfully!", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password reset token sent to email if it exists.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        // Doi mat khau thu hoi moi phien, ke ca phien dang goi request nay.
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.expired().toString())
                .body(new ApiResponse<>(true, "Password reset successfully!", null));
    }

    private ResponseEntity<ApiResponse<AuthResponse>> withRefreshCookie(AuthResult result, String message) {
        ResponseCookie cookie = refreshCookieFactory.create(result.refreshToken(), result.refreshExpiresAt());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ApiResponse<>(true, message, result.body()));
    }
}
