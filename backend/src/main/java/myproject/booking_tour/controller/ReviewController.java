package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.ReviewRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.ReviewResponse;
import myproject.booking_tour.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(@Valid @RequestBody ReviewRequest request, Authentication authentication) {
        String username = authentication.getName();
        ReviewResponse response = reviewService.addReview(request, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review added successfully!", response));
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByTour(@PathVariable Long tourId) {
        List<ReviewResponse> response = reviewService.getReviewsByTourId(tourId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reviews retrieved successfully!", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        reviewService.deleteReview(id, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review deleted successfully!", null));
    }
}
