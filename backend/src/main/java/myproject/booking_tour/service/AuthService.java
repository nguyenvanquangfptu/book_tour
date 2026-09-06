package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.LoginRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.response.AuthResult;
import myproject.booking_tour.security.ClientMetadata;

public interface AuthService {

    AuthResult register(RegisterRequest request, ClientMetadata client);

    AuthResult login(LoginRequest request, ClientMetadata client);

    AuthResult loginWithGoogle(String idToken, ClientMetadata client);

    /**
     * Doi refresh token lay access token moi + refresh token ke tiep.
     * Nem TokenReuseException neu token da tung duoc dung - xem RefreshTokenService.
     */
    AuthResult refresh(String refreshToken, ClientMetadata client);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    /** Thu hoi ca family refresh token cua phien dang cam token nay. */
    void logout(String refreshToken);
}
