package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.ChangePasswordRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.UserResponse;
import myproject.booking_tour.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Retrieved all users successfully!", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Retrieved user details successfully!", user));
    }

    private Long getAuthenticatedUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof myproject.booking_tour.security.CustomUserDetails) {
            return ((myproject.booking_tour.security.CustomUserDetails) authentication.getPrincipal()).getUser().getId();
        }
        throw new myproject.booking_tour.exception.BadRequestException("User is not authenticated");
    }

    // --- Customer APIs ---

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        Long userId = getAuthenticatedUserId();
        UserResponse profile = userService.getMyProfile(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully!", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(@Valid @RequestBody RegisterRequest request) {
        Long userId = getAuthenticatedUserId();
        UserResponse user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully!", user));
    }

    @PutMapping("/profile/change-password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getAuthenticatedUserId();
        userService.changePassword(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully!", null));
    }

    // --- Admin APIs ---

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfileAdmin(@PathVariable Long id, @Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.updateProfile(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully by admin!", user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully!", null));
    }
}
