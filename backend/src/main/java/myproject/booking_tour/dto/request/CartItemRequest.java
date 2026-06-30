package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CartItemRequest {
    @NotNull(message = "Tour ID is required")
    private Long tourId;

    @NotNull(message = "Guests number is required")
    @Min(value = 1, message = "Number of guests must be at least 1")
    private Integer guests;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}
