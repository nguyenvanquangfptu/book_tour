package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.request.TourRequest;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.dto.response.AccommodationResponse;
import myproject.booking_tour.dto.response.UtilityResponse;
import myproject.booking_tour.dto.TourItineraryDto;
import myproject.booking_tour.entity.TourItinerary;
import myproject.booking_tour.entity.Tour;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TourMapper {

    private final AccommodationMapper accommodationMapper;
    private final UtilityMapper utilityMapper;

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

        tour.setMaxPeople(request.getMaxPeople());
        tour.setAvailableSlots(request.getAvailableSlots());
        tour.setStatus(request.getStatus());
        tour.setTourType(request.getTourType());
        tour.setTransport(request.getTransport());
        tour.setHighlights(request.getHighlights());
        tour.setImages(request.getImages());
        if (request.getItinerary() != null) {
            List<TourItinerary> itineraryList = request.getItinerary().stream().map(dto -> {
                TourItinerary item = new TourItinerary();
                item.setDay(dto.getDay());
                item.setTitle(dto.getTitle());
                item.setDescription(dto.getDescription());
                return item;
            }).collect(Collectors.toList());
            tour.setItinerary(itineraryList);
        }
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

        response.setMaxPeople(tour.getMaxPeople());
        response.setAvailableSlots(tour.getAvailableSlots());
        response.setStatus(tour.getStatus());
        response.setTourType(tour.getTourType());
        response.setTransport(tour.getTransport());
        response.setBookedCount(tour.getBookedCount());
        response.setReviewCount(tour.getReviewCount());
        response.setRating(tour.getRating());

        boolean isAdmin = false;
        try {
            isAdmin = myproject.booking_tour.security.SecurityUtil.isAdmin();
        } catch (Exception e) {
            // Context might not be available in some background tasks
        }
        final boolean finalIsAdmin = isAdmin;

        // Map accommodations to List in TourResponse
        if (tour.getAccommodations() != null && !tour.getAccommodations().isEmpty()) {
            List<AccommodationResponse> accommodationList = tour.getAccommodations().stream()
                .filter(acc -> finalIsAdmin || Boolean.TRUE.equals(acc.getIsActive()))
                .map(accommodationMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
            response.setAccommodations(accommodationList);
        }

        // Map utilities set to utilities List in TourResponse
        if (tour.getUtilities() != null) {
            List<UtilityResponse> utilityList = tour.getUtilities().stream()
                    .filter(u -> finalIsAdmin || Boolean.TRUE.equals(u.getIsActive()))
                    .map(utilityMapper::toResponse)
                    .collect(Collectors.toList());
            response.setUtilities(utilityList);
        }

        // Map new detail fields
        response.setHighlights(tour.getHighlights());
        response.setImages(tour.getImages());
        if (tour.getItinerary() != null) {
            List<TourItineraryDto> dtoList = tour.getItinerary().stream().map(item -> {
                TourItineraryDto dto = new TourItineraryDto();
                dto.setDay(item.getDay());
                dto.setTitle(item.getTitle());
                dto.setDescription(item.getDescription());
                return dto;
            }).collect(Collectors.toList());
            response.setItinerary(dtoList);
        }

        return response;
    }
}
