package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.AccommodationRequest;
import myproject.booking_tour.dto.response.AccommodationResponse;

import java.util.List;

public interface AccommodationService {
    List<AccommodationResponse> getAllAccommodations();
    AccommodationResponse getAccommodationById(Long id);
    AccommodationResponse createAccommodation(AccommodationRequest request);
    AccommodationResponse updateAccommodation(Long id, AccommodationRequest request);
    void deleteAccommodation(Long id);
}
