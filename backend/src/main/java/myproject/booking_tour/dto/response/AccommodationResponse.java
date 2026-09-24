package myproject.booking_tour.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationResponse {
    private Long id;
    private String name;
    private String type;
    private String address;
    private String description;
    private Boolean isActive;
}
