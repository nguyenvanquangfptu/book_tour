package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.TourRequest;
import myproject.booking_tour.dto.response.PageResponse;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.dto.response.PopularDestinationResponse;

import java.util.List;

public interface TourService {
    List<TourResponse> getAllTours();
    TourResponse getTourById(Long id);
    List<TourResponse> searchTours(String keyword);
    PageResponse<TourResponse> searchAndFilterTours(String keyword, String destination, java.time.LocalDate startDate, java.time.LocalDate endDate, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String status, List<String> tourTypes, List<String> transports, int page, int size, String sortBy, String sortDir);
    TourResponse createTour(TourRequest request);
    TourResponse updateTour(Long id, TourRequest request);
    void deleteTour(Long id);
    List<PopularDestinationResponse> getPopularDestinations(int limit);
}
