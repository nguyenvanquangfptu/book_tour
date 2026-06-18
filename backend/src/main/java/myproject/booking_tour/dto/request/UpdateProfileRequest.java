package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|vn|net|org|edu|gov|info|io)$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @jakarta.validation.constraints.Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Invalid phone number format")
    private String phone;
}
