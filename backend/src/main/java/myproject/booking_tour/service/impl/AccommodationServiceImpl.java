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

@Service
public class AccommodationServiceImpl implements AccommodationService {

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private AccommodationMapper accommodationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AccommodationResponse> getAllAccommodations() {
        return accommodationRepository.findAll().stream()
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
    public AccommodationResponse updateAccommodation(Long id, AccommodationRequest request) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation not found with id: " + id));

        accommodation.setName(request.getName());
        accommodation.setType(request.getType());
        accommodation.setAddress(request.getAddress());
        accommodation.setDescription(request.getDescription());

        Accommodation updated = accommodationRepository.save(accommodation);
        return accommodationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAccommodation(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation not found with id: " + id));
        accommodationRepository.delete(accommodation);
    }
}
