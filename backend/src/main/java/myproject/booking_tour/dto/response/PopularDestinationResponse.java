package myproject.booking_tour.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularDestinationResponse {
    private String name;
    private Long count;
    private String image;
}
