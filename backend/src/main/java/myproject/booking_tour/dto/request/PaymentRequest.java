package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private String paymentMethod; // Using String to match Payment.java field
}
