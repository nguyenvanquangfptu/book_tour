package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.ChangePasswordRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse getMyProfile(Long userId);
    UserResponse updateProfile(Long userId, RegisterRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);

    UserResponse updateAvatar(Long userId, String avatarUrl);

    void deleteUser(Long id);
}
