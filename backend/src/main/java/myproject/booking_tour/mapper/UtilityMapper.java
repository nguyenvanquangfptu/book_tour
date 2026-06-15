package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.request.UtilityRequest;
import myproject.booking_tour.dto.response.UtilityResponse;
import myproject.booking_tour.entity.Utility;
import org.springframework.stereotype.Component;

@Component
public class UtilityMapper {

    public Utility toEntity(UtilityRequest request) {
        if (request == null) {
            return null;
        }
        Utility utility = new Utility();
        utility.setName(request.getName());
        utility.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            utility.setIsActive(request.getIsActive());
        }
        return utility;
    }

    public UtilityResponse toResponse(Utility utility) {
        if (utility == null) {
            return null;
        }
        UtilityResponse response = new UtilityResponse();
        response.setId(utility.getId());
        response.setName(utility.getName());
        response.setDescription(utility.getDescription());
        response.setIsActive(utility.getIsActive());
        return response;
    }
}
