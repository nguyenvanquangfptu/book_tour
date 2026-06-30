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
        response.setTravelDate(booking.getTravelDate());

        try {
            if (booking.getUser() != null) {
                response.setUserId(booking.getUser().getId());
                response.setCustomerName(booking.getUser().getFullName());
            }
        } catch (jakarta.persistence.EntityNotFoundException e) {
            response.setCustomerName("Người dùng đã bị xóa");
        } catch (Exception e) {
            response.setCustomerName("Lỗi tải thông tin");
        }

        try {
            if (booking.getTour() != null) {
                response.setTourId(booking.getTour().getId());
                response.setTourTitle(booking.getTour().getTitle());
                response.setDestination(booking.getTour().getDestination());
            }
        } catch (jakarta.persistence.EntityNotFoundException e) {
            response.setTourTitle("Tour đã bị xóa");
        } catch (Exception e) {
            response.setTourTitle("Lỗi tải thông tin");
        }

        response.setCustomerEmail(booking.getCustomerEmail());
        response.setCustomerPhone(booking.getCustomerPhone());
        response.setNote(booking.getNote());
        
        try {
            if (booking.getUser() != null && booking.getTour() != null) {
                Long tourId = booking.getTour().getId();
                Long userId = booking.getUser().getId();
                Review review = reviewRepository.findByTourId(tourId).stream()
                        .filter(r -> r.getUser() != null && r.getUser().getId().equals(userId))
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
        } catch (Exception e) {
            response.setReviewed(false);
        }

        return response;
    }
}
