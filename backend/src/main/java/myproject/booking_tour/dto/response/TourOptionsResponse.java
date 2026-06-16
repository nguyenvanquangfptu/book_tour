package myproject.booking_tour.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourOptionsResponse {
    private List<String> destinations;
    private List<String> tourTypes;
    private List<String> transports;
}
