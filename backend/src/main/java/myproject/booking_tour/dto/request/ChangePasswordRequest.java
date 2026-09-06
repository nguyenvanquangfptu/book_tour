package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.NotBlank;
import myproject.booking_tour.validation.StrongPassword;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @StrongPassword
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
