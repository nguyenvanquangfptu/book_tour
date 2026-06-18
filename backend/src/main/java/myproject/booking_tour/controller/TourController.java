package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.TourRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.dto.response.PageResponse;
import myproject.booking_tour.dto.response.PopularDestinationResponse;
import myproject.booking_tour.service.TourService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TourResponse>>> getAllTours() {
        List<TourResponse> tours = tourService.getAllTours();
        return ResponseEntity.ok(new ApiResponse<>(true, "Tours retrieved successfully!", tours));
    }

    @GetMapping("/popular-destinations")
    public ResponseEntity<ApiResponse<List<PopularDestinationResponse>>> getPopularDestinations(
            @RequestParam(defaultValue = "4") int limit) {
        List<PopularDestinationResponse> destinations = tourService.getPopularDestinations(limit);
        return ResponseEntity.ok(new ApiResponse<>(true, "Popular destinations retrieved successfully!", destinations));
    }

    @GetMapping("/options")
    public ResponseEntity<ApiResponse<myproject.booking_tour.dto.response.TourOptionsResponse>> getTourOptions() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tour options fetched successfully", tourService.getTourOptions()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TourResponse>> getTourById(@PathVariable Long id) {
        TourResponse tour = tourService.getTourById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tour details retrieved successfully!", tour));
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<ApiResponse<Integer>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        Integer availableSlots = tourService.getAvailableSlots(id, date);
        return ResponseEntity.ok(new ApiResponse<>(true, "Available slots retrieved successfully!", availableSlots));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<TourResponse>>> searchAndFilterTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String destination,

            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.util.List<String> tourTypes,
            @RequestParam(required = false) java.util.List<String> transports,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        myproject.booking_tour.dto.response.PageResponse<TourResponse> response = tourService.searchAndFilterTours(keyword, destination, minPrice, maxPrice, status, tourTypes, transports, page, size, sortBy, sortDir);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tours retrieved successfully!", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TourResponse>> createTour(@Valid @RequestBody TourRequest request) {
        TourResponse tour = tourService.createTour(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tour created successfully!", tour));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TourResponse>> updateTour(@PathVariable Long id, @Valid @RequestBody TourRequest request) {
        TourResponse tour = tourService.updateTour(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tour updated successfully!", tour));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tour deleted successfully!", null));
    }
}
