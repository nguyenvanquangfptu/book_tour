package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.AccommodationRequest;
import myproject.booking_tour.dto.response.AccommodationResponse;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.service.AccommodationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccommodationResponse>>> getAllAccommodations() {
        List<AccommodationResponse> list = accommodationService.getAllAccommodations();
        return ResponseEntity.ok(new ApiResponse<>(true, "Accommodations retrieved successfully!", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccommodationResponse>> getAccommodationById(@PathVariable Long id) {
        AccommodationResponse response = accommodationService.getAccommodationById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Accommodation details retrieved successfully!", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccommodationResponse>> createAccommodation(@Valid @RequestBody AccommodationRequest request) {
        AccommodationResponse response = accommodationService.createAccommodation(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Accommodation created successfully!", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccommodationResponse>> updateAccommodation(@PathVariable Long id, @Valid @RequestBody AccommodationRequest request) {
        AccommodationResponse response = accommodationService.updateAccommodation(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Accommodation updated successfully!", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccommodation(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Accommodation deleted successfully!", null));
    }
}
