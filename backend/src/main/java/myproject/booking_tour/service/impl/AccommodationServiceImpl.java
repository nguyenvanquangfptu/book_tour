package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.AccommodationRequest;
import myproject.booking_tour.dto.response.AccommodationResponse;
import myproject.booking_tour.entity.Accommodation;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.AccommodationMapper;
import myproject.booking_tour.repository.AccommodationRepository;
import myproject.booking_tour.service.AccommodationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final myproject.booking_tour.repository.TourRepository tourRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccommodationResponse> getAllAccommodations() {
        boolean isAdmin = myproject.booking_tour.security.SecurityUtil.isAdmin();
        return accommodationRepository.findAll().stream()
                .filter(acc -> isAdmin || Boolean.TRUE.equals(acc.getIsActive()))
                .map(accommodationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccommodationResponse getAccommodationById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation not found with id: " + id));
        return accommodationMapper.toResponse(accommodation);
    }

    @Override
    @Transactional
    public AccommodationResponse createAccommodation(AccommodationRequest request) {
        Accommodation accommodation = accommodationMapper.toEntity(request);
        Accommodation saved = accommodationRepository.save(accommodation);
        return accommodationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"tourOptions", "popularDestinations"}, allEntries = true)
    public AccommodationResponse updateAccommodation(Long id, AccommodationRequest request) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation not found with id: " + id));

        accommodation.setName(request.getName());
        accommodation.setType(request.getType());
        accommodation.setAddress(request.getAddress());
        accommodation.setDescription(request.getDescription());
        boolean wasActive = accommodation.getIsActive() != null ? accommodation.getIsActive() : true;
        
        if (request.getIsActive() != null) {
            accommodation.setIsActive(request.getIsActive());
            
            // If changing from active to inactive, set all associated tours to INACTIVE
            if (wasActive && !request.getIsActive()) {
                List<myproject.booking_tour.entity.Tour> associatedTours = tourRepository.findByAccommodations_Id(id);
                for (myproject.booking_tour.entity.Tour tour : associatedTours) {
                    tour.setStatus("INACTIVE");
                }
                tourRepository.saveAll(associatedTours);
            }
        }

        Accommodation updated = accommodationRepository.save(accommodation);
        return accommodationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAccommodation(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation not found with id: " + id));
        
        if (tourRepository.existsByAccommodations_Id(id)) {
            throw new RuntimeException("Nơi lưu trú này đang được sử dụng trong Tour. Không thể xóa, vui lòng chuyển trạng thái sang Không hoạt động (isActive = false).");
        }
        
        accommodationRepository.delete(accommodation);
    }
}
