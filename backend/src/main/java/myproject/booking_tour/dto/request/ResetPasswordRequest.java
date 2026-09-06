package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.NotBlank;
import myproject.booking_tour.validation.StrongPassword;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @StrongPassword
    private String newPassword;
}
