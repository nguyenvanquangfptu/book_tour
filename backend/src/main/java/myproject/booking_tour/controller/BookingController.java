package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.BookingRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.BookingResponse;
import myproject.booking_tour.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    private Long getAuthenticatedUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof myproject.booking_tour.security.CustomUserDetails) {
            return ((myproject.booking_tour.security.CustomUserDetails) authentication.getPrincipal()).getUser().getId();
        }
        throw new myproject.booking_tour.exception.BadRequestException("User is not authenticated");
    }

    // --- Customer APIs ---

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request) {
        Long userId = getAuthenticatedUserId();
        BookingResponse response = bookingService.createBooking(request, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Booking created successfully!", response));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
        Long userId = getAuthenticatedUserId();
        List<BookingResponse> list = bookingService.getMyBookings(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "My bookings retrieved successfully!", list));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        BookingResponse response = bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Booking cancelled successfully!", response));
    }

    // --- Admin APIs ---

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> list = bookingService.getAllBookings();
        return ResponseEntity.ok(new ApiResponse<>(true, "All bookings retrieved successfully!", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Booking details retrieved successfully!", response));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Booking confirmed successfully!", response));
    }
}
