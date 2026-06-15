package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.UtilityRequest;
import myproject.booking_tour.dto.response.UtilityResponse;
import myproject.booking_tour.entity.Utility;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.UtilityMapper;
import myproject.booking_tour.repository.UtilityRepository;
import myproject.booking_tour.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UtilityServiceImpl implements UtilityService {

    @Autowired
    private UtilityRepository utilityRepository;

    @Autowired
    private UtilityMapper utilityMapper;

    @Autowired
    private myproject.booking_tour.repository.TourRepository tourRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UtilityResponse> getAllUtilities() {
        return utilityRepository.findAll().stream()
                .map(utilityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UtilityResponse getUtilityById(Long id) {
        Utility utility = utilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utility not found with id: " + id));
        return utilityMapper.toResponse(utility);
    }

    @Override
    @Transactional
    public UtilityResponse createUtility(UtilityRequest request) {
        Utility utility = utilityMapper.toEntity(request);
        Utility saved = utilityRepository.save(utility);
        return utilityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UtilityResponse updateUtility(Long id, UtilityRequest request) {
        Utility utility = utilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utility not found with id: " + id));

        utility.setName(request.getName());
        utility.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            utility.setIsActive(request.getIsActive());
        }

        Utility updated = utilityRepository.save(utility);
        return utilityMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUtility(Long id) {
        Utility utility = utilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utility not found with id: " + id));
        
        if (tourRepository.existsByUtilityId(id)) {
            throw new RuntimeException("Tiện ích này đang được sử dụng trong Tour. Không thể xóa, vui lòng chuyển trạng thái sang Không hoạt động (isActive = false).");
        }
        
        utilityRepository.delete(utility);
    }
}
