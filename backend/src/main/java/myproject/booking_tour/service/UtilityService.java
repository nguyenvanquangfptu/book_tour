package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.UtilityRequest;
import myproject.booking_tour.dto.response.UtilityResponse;

import java.util.List;

public interface UtilityService {
    List<UtilityResponse> getAllUtilities();
    UtilityResponse getUtilityById(Long id);
    UtilityResponse createUtility(UtilityRequest request);
    UtilityResponse updateUtility(Long id, UtilityRequest request);
    void deleteUtility(Long id);
}
