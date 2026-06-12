package myproject.booking_tour.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
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
}
