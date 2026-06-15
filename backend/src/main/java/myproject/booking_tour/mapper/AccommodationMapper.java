package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.request.AccommodationRequest;
import myproject.booking_tour.dto.response.AccommodationResponse;
import myproject.booking_tour.entity.Accommodation;
import org.springframework.stereotype.Component;

@Component
public class AccommodationMapper {

    public Accommodation toEntity(AccommodationRequest request) {
        if (request == null) {
            return null;
        }
        Accommodation accommodation = new Accommodation();
        accommodation.setName(request.getName());
        accommodation.setType(request.getType());
        accommodation.setAddress(request.getAddress());
        accommodation.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            accommodation.setIsActive(request.getIsActive());
        }
        return accommodation;
    }

    public AccommodationResponse toResponse(Accommodation accommodation) {
        if (accommodation == null) {
            return null;
        }
        AccommodationResponse response = new AccommodationResponse();
        response.setId(accommodation.getId());
        response.setName(accommodation.getName());
        response.setType(accommodation.getType());
        response.setAddress(accommodation.getAddress());
        response.setDescription(accommodation.getDescription());
        response.setIsActive(accommodation.getIsActive());
        return response;
    }
}
