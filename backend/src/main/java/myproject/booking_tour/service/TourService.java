package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.TourRequest;
import myproject.booking_tour.dto.response.PageResponse;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.dto.response.PopularDestinationResponse;

import java.math.BigDecimal;
import java.util.List;

public interface TourService {

    TourResponse getTourById(Long id);
    List<TourResponse> searchTours(String keyword);
    PageResponse<TourResponse> searchAndFilterTours(String keyword, String destination, Integer durationDays, Integer guests, BigDecimal minPrice, BigDecimal maxPrice, String status, List<String> tourTypes, List<String> transports, int page, int size, String sortBy, String sortDir);
    TourResponse createTour(TourRequest request);
    TourResponse updateTour(Long id, TourRequest request);
    void deleteTour(Long id);
    List<PopularDestinationResponse> getPopularDestinations(int limit);
    Integer getAvailableSlots(Long id, java.time.LocalDate date);
    myproject.booking_tour.dto.response.TourOptionsResponse getTourOptions();
}
