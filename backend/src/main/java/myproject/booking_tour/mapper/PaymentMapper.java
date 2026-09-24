package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.response.PaymentResponse;
import myproject.booking_tour.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setPaymentDate(payment.getPaymentDate());

        if (payment.getBooking() != null) {
            response.setBookingId(payment.getBooking().getId());
        }

        return response;
    }
}
