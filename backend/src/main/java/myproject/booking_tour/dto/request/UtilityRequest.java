package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityRequest {
    @NotBlank(message = "Utility name is required")
    private String name;

    private String description;
    
    private Boolean isActive = true;
}
