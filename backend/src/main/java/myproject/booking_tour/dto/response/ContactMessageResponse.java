package myproject.booking_tour.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ContactMessageResponse {
    private Long id;
    private String fullName;
    private String email;
    private String subject;
    private String message;
    private String status;
    private LocalDateTime createdAt;
}
