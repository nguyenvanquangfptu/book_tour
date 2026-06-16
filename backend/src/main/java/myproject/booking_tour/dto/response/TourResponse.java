package myproject.booking_tour.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import myproject.booking_tour.entity.TourItinerary;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourResponse {
    private Long id;
    private String title;
    private String destination;
    private String description;
    private BigDecimal price;
    private String duration;
    private String imageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxPeople;
    private Integer availableSlots;
    private String status;
    private String tourType;
    private String transport;
    private Integer bookedCount;
    private Integer reviewCount;
    private Double rating;
    private List<AccommodationResponse> accommodations;
    private List<UtilityResponse> utilities;
    private List<String> highlights;
    private List<String> images;
    private List<TourItinerary> itinerary;
}
