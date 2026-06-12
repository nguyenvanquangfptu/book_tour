package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.ReviewRequest;
import myproject.booking_tour.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse addReview(ReviewRequest request, String username);
    List<ReviewResponse> getReviewsByTourId(Long tourId);
    void deleteReview(Long id, String username);
}
