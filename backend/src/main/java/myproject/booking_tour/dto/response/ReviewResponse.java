package myproject.booking_tour.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long tourId;
    private Long userId;
    private String username;
    private String fullName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
