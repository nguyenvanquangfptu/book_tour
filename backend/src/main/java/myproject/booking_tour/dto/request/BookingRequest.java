package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    @NotNull(message = "Tour ID is required")
    private Long tourId;

    @NotNull(message = "Number of people is required")
    @Min(value = 1, message = "Number of people must be at least 1")
    private Integer numberOfPeople;
}
