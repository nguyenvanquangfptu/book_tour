package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.UtilityRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.UtilityResponse;
import myproject.booking_tour.service.UtilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilities")
@RequiredArgsConstructor
public class UtilityController {

    private final UtilityService utilityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UtilityResponse>>> getAllUtilities() {
        List<UtilityResponse> list = utilityService.getAllUtilities();
        return ResponseEntity.ok(new ApiResponse<>(true, "Utilities retrieved successfully!", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UtilityResponse>> getUtilityById(@PathVariable Long id) {
        UtilityResponse response = utilityService.getUtilityById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Utility details retrieved successfully!", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UtilityResponse>> createUtility(@Valid @RequestBody UtilityRequest request) {
        UtilityResponse response = utilityService.createUtility(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Utility created successfully!", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UtilityResponse>> updateUtility(@PathVariable Long id, @Valid @RequestBody UtilityRequest request) {
        UtilityResponse response = utilityService.updateUtility(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Utility updated successfully!", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUtility(@PathVariable Long id) {
        utilityService.deleteUtility(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Utility deleted successfully!", null));
    }
}
