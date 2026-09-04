package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.ReviewRequest;
import myproject.booking_tour.dto.response.ReviewResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Review;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.mapper.ReviewMapper;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.ReviewRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User mockUser;
    private Tour mockTour;
    private Review mockReview;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockTour = new Tour();
        mockTour.setId(10L);

        mockReview = new Review();
        mockReview.setId(100L);
        mockReview.setUser(mockUser);
        mockReview.setTour(mockTour);
        mockReview.setRating(5);
    }

    @Test
    void addReview_ShouldThrowException_WhenUserHasNotBooked() {
        ReviewRequest request = new ReviewRequest();
        request.setTourId(10L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> reviewService.addReview(request, "testuser"));
    }

    @Test
    void addReview_ShouldSave_WhenValid() {
        ReviewRequest request = new ReviewRequest();
        request.setTourId(10L);
        request.setRating(5);

        Booking mockBooking = new Booking();
        mockBooking.setTour(mockTour);
        mockBooking.setStatus("COMPLETED");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(mockBooking));
        when(reviewRepository.findByTourId(10L)).thenReturn(List.of());

        when(reviewMapper.toEntity(request)).thenReturn(mockReview);
        when(reviewRepository.save(mockReview)).thenReturn(mockReview);
        when(reviewMapper.toResponse(mockReview)).thenReturn(new ReviewResponse());

        ReviewResponse response = reviewService.addReview(request, "testuser");

        assertNotNull(response);
        verify(reviewRepository, times(1)).save(mockReview);
        // tours.rating / review_count gio la @Formula, tinh truc tiep tu bang
        // reviews -> them danh gia KHONG con ghi gi vao bang tours nua.
        verify(tourRepository, never()).save(any());
    }
}
