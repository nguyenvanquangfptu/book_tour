package myproject.booking_tour.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long userId;
    private String customerName;
    private Long tourId;
    private String tourTitle;
    private Integer numberOfPeople;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime bookingDate;
    private LocalDate travelDate;
    
    private boolean isReviewed;
    private Long reviewId;
    private Integer reviewRating;
    private String reviewComment;
}
