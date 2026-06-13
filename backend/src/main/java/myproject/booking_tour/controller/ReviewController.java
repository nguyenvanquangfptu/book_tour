package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.ReviewRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.ReviewResponse;
import myproject.booking_tour.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(@Valid @RequestBody ReviewRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ReviewResponse response = reviewService.addReview(request, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review added successfully!", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ReviewResponse response = reviewService.updateReview(id, request, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review updated successfully!", response));
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByTour(@PathVariable Long tourId) {
        List<ReviewResponse> response = reviewService.getReviewsByTourId(tourId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reviews retrieved successfully!", response));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getRecentReviews() {
        List<ReviewResponse> response = reviewService.getRecentReviews();
        return ResponseEntity.ok(new ApiResponse<>(true, "Recent reviews retrieved successfully!", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        reviewService.deleteReview(id, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review deleted successfully!", null));
    }
}
