package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.request.TourRequest;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.dto.response.AccommodationResponse;
import myproject.booking_tour.dto.response.UtilityResponse;
import myproject.booking_tour.entity.Tour;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TourMapper {

    @Autowired
    private AccommodationMapper accommodationMapper;

    @Autowired
    private UtilityMapper utilityMapper;

    public Tour toEntity(TourRequest request) {
        if (request == null) {
            return null;
        }
        Tour tour = new Tour();
        tour.setTitle(request.getTitle());
        tour.setDestination(request.getDestination());
        tour.setDescription(request.getDescription());
        tour.setPrice(request.getPrice());
        tour.setDuration(request.getDuration());
        tour.setImageUrl(request.getImageUrl());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());
        tour.setMaxPeople(request.getMaxPeople());
        tour.setAvailableSlots(request.getAvailableSlots());
        tour.setStatus(request.getStatus());
        tour.setTourType(request.getTourType());
        tour.setTransport(request.getTransport());
        tour.setHighlights(request.getHighlights());
        tour.setImages(request.getImages());
        tour.setItinerary(request.getItinerary());
        // Note: accommodation and utilities entities are loaded and linked in the Service layer
        return tour;
    }

    public TourResponse toResponse(Tour tour) {
        if (tour == null) {
            return null;
        }
        TourResponse response = new TourResponse();
        response.setId(tour.getId());
        response.setTitle(tour.getTitle());
        response.setDestination(tour.getDestination());
        response.setDescription(tour.getDescription());
        response.setPrice(tour.getPrice());
        response.setDuration(tour.getDuration());
        response.setImageUrl(tour.getImageUrl());
        response.setStartDate(tour.getStartDate());
        response.setEndDate(tour.getEndDate());
        response.setMaxPeople(tour.getMaxPeople());
        response.setAvailableSlots(tour.getAvailableSlots());
        response.setStatus(tour.getStatus());
        response.setTourType(tour.getTourType());
        response.setTransport(tour.getTransport());
        response.setBookedCount(tour.getBookedCount());
        response.setReviewCount(tour.getReviewCount());
        response.setRating(tour.getRating());

        // Map single accommodation to accommodations List in TourResponse
        if (tour.getAccommodation() != null) {
            List<AccommodationResponse> accommodationList = new ArrayList<>();
            accommodationList.add(accommodationMapper.toResponse(tour.getAccommodation()));
            response.setAccommodations(accommodationList);
        }

        // Map utilities set to utilities List in TourResponse
        if (tour.getUtilities() != null) {
            List<UtilityResponse> utilityList = tour.getUtilities().stream()
                    .map(utilityMapper::toResponse)
                    .collect(Collectors.toList());
            response.setUtilities(utilityList);
        }

        // Map new detail fields
        response.setHighlights(tour.getHighlights());
        response.setImages(tour.getImages());
        response.setItinerary(tour.getItinerary());

        return response;
    }
}
