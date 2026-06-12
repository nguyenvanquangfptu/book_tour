package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationRequest {
    @NotBlank(message = "Accommodation name is required")
    private String name;

    private String type; // Using String to match Accommodation.java type field

    @NotBlank(message = "Address is required")
    private String address;

    private String description;
}
