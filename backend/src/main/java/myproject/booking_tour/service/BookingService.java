package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.BookingRequest;
import myproject.booking_tour.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, Long userId);
    List<BookingResponse> getMyBookings(Long userId);
    myproject.booking_tour.dto.response.PageResponse<BookingResponse> getAllBookings(int page, int size);
    BookingResponse getBookingById(Long id);
    BookingResponse confirmBooking(Long id);
    BookingResponse cancelBooking(Long id, Long userId);
}
