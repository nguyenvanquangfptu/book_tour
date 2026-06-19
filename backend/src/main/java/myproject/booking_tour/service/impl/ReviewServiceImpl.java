package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.ReviewRequest;
import myproject.booking_tour.dto.response.ReviewResponse;
import myproject.booking_tour.entity.Review;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.ReviewMapper;
import myproject.booking_tour.repository.ReviewRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.UserRepository;
import myproject.booking_tour.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final myproject.booking_tour.repository.BookingRepository bookingRepository;

    @Override
    @Transactional
    public ReviewResponse addReview(ReviewRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + request.getTourId()));

        // Check if user has actually booked and completed/confirmed the tour
        boolean hasBooked = bookingRepository.findByUserId(user.getId()).stream()
                .anyMatch(b -> b.getTour() != null && b.getTour().getId().equals(tour.getId()) && 
                        ("CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus())));

        if (!hasBooked) {
            throw new BadRequestException("Bạn phải đặt và hoàn thành tour này mới có thể đánh giá!");
        }

        // Check if user already reviewed this tour
        boolean alreadyReviewed = reviewRepository.findByTourId(tour.getId()).stream()
                .anyMatch(r -> r.getUser().getId().equals(user.getId()));
        
        if (alreadyReviewed) {
            throw new BadRequestException("Bạn đã đánh giá tour này rồi!");
        }

        Review review = reviewMapper.toEntity(request);
        review.setUser(user);
        review.setTour(tour);

        Review savedReview = reviewRepository.save(review);
        updateTourRating(tour);
        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, ReviewRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(user.getId()) && !user.getRole().getName().equals("ADMIN")) {
            throw new BadRequestException("You don't have permission to edit this review");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        Review savedReview = reviewRepository.save(review);
        updateTourRating(review.getTour());
        return reviewMapper.toResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getReviewsByTourId(Long tourId) {
        return reviewRepository.findByTourId(tourId).stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getRecentReviews() {
        return reviewRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteReview(Long id, String username) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!review.getUser().getId().equals(user.getId()) && !user.getRole().getName().equals("ADMIN")) {
            throw new BadRequestException("You don't have permission to delete this review");
        }

        Tour tour = review.getTour();
        reviewRepository.delete(review);
        updateTourRating(tour);
    }

    private void updateTourRating(Tour tour) {
        List<Review> reviews = reviewRepository.findByTourId(tour.getId());
        if (reviews == null || reviews.isEmpty()) {
            tour.setRating(0.0);
            tour.setReviewCount(0);
        } else {
            double avg = reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
            tour.setRating(Math.round(avg * 10.0) / 10.0);
            tour.setReviewCount(reviews.size());
        }
        tourRepository.save(tour);
    }
}
