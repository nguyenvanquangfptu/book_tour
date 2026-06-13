package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.response.BookingResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Review;
import myproject.booking_tour.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    @Autowired
    private ReviewRepository reviewRepository;

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
        
        if (booking.getUser() != null && booking.getTour() != null) {
            Review review = reviewRepository.findByTourId(booking.getTour().getId()).stream()
                    .filter(r -> r.getUser().getId().equals(booking.getUser().getId()))
                    .findFirst().orElse(null);
            if (review != null) {
                response.setReviewed(true);
                response.setReviewId(review.getId());
                response.setReviewRating(review.getRating());
                response.setReviewComment(review.getComment());
            } else {
                response.setReviewed(false);
            }
        }

        return response;
    }
}
