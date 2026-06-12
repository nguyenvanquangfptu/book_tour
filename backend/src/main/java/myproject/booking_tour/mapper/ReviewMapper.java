package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.request.ReviewRequest;
import myproject.booking_tour.dto.response.ReviewResponse;
import myproject.booking_tour.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toEntity(ReviewRequest request) {
        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return review;
    }

    public ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        if (review.getTour() != null) {
            response.setTourId(review.getTour().getId());
        }
        if (review.getUser() != null) {
            response.setUserId(review.getUser().getId());
            response.setUsername(review.getUser().getUsername());
            response.setFullName(review.getUser().getFullName());
        }
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
