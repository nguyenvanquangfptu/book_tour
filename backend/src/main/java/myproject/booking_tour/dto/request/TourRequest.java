package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import myproject.booking_tour.entity.TourItinerary;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String destination;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private String duration;

    private String imageUrl;



    @Min(value = 1, message = "Max people must be at least 1")
    private Integer maxPeople;

    private Integer availableSlots;

    private String status;

    private String tourType;

    private String transport;

    private Set<Long> accommodationIds;

    private Set<Long> utilityIds;

    private List<String> highlights;

    private List<String> images;

    private List<TourItinerary> itinerary;
}
