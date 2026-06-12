package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.LoginRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse loginWithGoogle(String idToken);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
