package myproject.booking_tour.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityResponse {
    private Long id;
    private String name;
    private String description;
}
