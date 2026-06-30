package myproject.booking_tour.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CartItemResponse {
    private Long id;
    private Long tourId;
    private String tourTitle;
    private BigDecimal price;
    private String imageUrl;
    private Integer guests;
    private LocalDate startDate;
}
