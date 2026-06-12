package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.response.BookingResponse;
import myproject.booking_tour.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setNumberOfPeople(booking.getNumberOfPeople());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus());
        response.setBookingDate(booking.getBookingDate());

        if (booking.getUser() != null) {
            response.setUserId(booking.getUser().getId());
            response.setCustomerName(booking.getUser().getFullName());
        }

        if (booking.getTour() != null) {
            response.setTourId(booking.getTour().getId());
            response.setTourTitle(booking.getTour().getTitle());
        }

        return response;
    }
}
