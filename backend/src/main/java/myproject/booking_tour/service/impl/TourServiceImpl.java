package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.TourRequest;
import myproject.booking_tour.dto.response.PageResponse;
import myproject.booking_tour.dto.response.PageResponse;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.dto.response.PopularDestinationResponse;
import myproject.booking_tour.entity.Accommodation;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.Utility;
import myproject.booking_tour.entity.TourSchedule;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.TourMapper;
import myproject.booking_tour.repository.AccommodationRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.TourScheduleRepository;
import myproject.booking_tour.repository.UtilityRepository;
import myproject.booking_tour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TourServiceImpl implements TourService {

    @Autowired
    private TourRepository tourRepository;

    @Override
    @org.springframework.cache.annotation.Cacheable("popularDestinations")
    public List<PopularDestinationResponse> getPopularDestinations(int limit) {
        return tourRepository.findPopularDestinations(PageRequest.of(0, limit));
    }

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private UtilityRepository utilityRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private TourMapper tourMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TourResponse> getAllTours() {
        return tourRepository.findAll().stream()
                .filter(t -> !"DELETED".equals(t.getStatus()))
                .map(tourMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TourResponse getTourById(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        return tourMapper.toResponse(tour);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourResponse> searchTours(String keyword) {
        return tourRepository.findByTitleContainingIgnoreCaseAndStatusNot(keyword, "DELETED").stream()
                .map(tourMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TourResponse> searchAndFilterTours(String keyword, String destination, LocalDate startDate, LocalDate endDate, BigDecimal minPrice, BigDecimal maxPrice, String status, List<String> tourTypes, List<String> transports, int page, int size, String sortBy, String sortDir) {
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        org.springframework.data.jpa.domain.Specification<Tour> spec = myproject.booking_tour.repository.specification.TourSpecification.filterTours(keyword, destination, startDate, endDate, minPrice, maxPrice, status, tourTypes, transports);
        org.springframework.data.domain.Page<Tour> tours = tourRepository.findAll(spec, pageable);
        List<TourResponse> content = tours.getContent().stream().map(tourMapper::toResponse).collect(Collectors.toList());
        return PageResponse.<TourResponse>builder()
                .pageNumber(tours.getNumber())
                .pageSize(tours.getSize())
                .totalElements(tours.getTotalElements())
                .totalPages(tours.getTotalPages())
                .isLast(tours.isLast())
                .content(content)
                .build();
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"tourOptions", "popularDestinations"}, allEntries = true)
    public TourResponse createTour(TourRequest request) {
        Tour tour = tourMapper.toEntity(request);

        // Map Single Accommodation from set in Request
        if (request.getAccommodationIds() != null && !request.getAccommodationIds().isEmpty()) {
            Long accId = request.getAccommodationIds().iterator().next();
            Accommodation accommodation = accommodationRepository.findById(accId)
                    .orElseThrow(() -> new ResourceNotFoundException("Accommodation not found with id: " + accId));
            tour.setAccommodation(accommodation);
        }

        // Map Utilities from set in Request
        if (request.getUtilityIds() != null && !request.getUtilityIds().isEmpty()) {
            Set<Utility> utilities = new HashSet<>(utilityRepository.findAllById(request.getUtilityIds()));
            tour.setUtilities(utilities);
        }

        if (tour.getAvailableSlots() == null) {
            tour.setAvailableSlots(tour.getMaxPeople() != null ? tour.getMaxPeople() : 0);
        }

        Tour savedTour = tourRepository.save(tour);
        return tourMapper.toResponse(savedTour);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"tourOptions", "popularDestinations"}, allEntries = true)
    public TourResponse updateTour(Long id, TourRequest request) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));

        tour.setTitle(request.getTitle());
        tour.setDestination(request.getDestination());
        tour.setDescription(request.getDescription());
        tour.setPrice(request.getPrice());
        tour.setDuration(request.getDuration());
        tour.setImageUrl(request.getImageUrl());
        tour.setImages(request.getImages());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());
        tour.setMaxPeople(request.getMaxPeople());
        tour.setTourType(request.getTourType());
        tour.setTransport(request.getTransport());
        tour.setHighlights(request.getHighlights());
        
        if (request.getItinerary() != null) {
            tour.getItinerary().clear();
            tour.getItinerary().addAll(request.getItinerary());
        }

        // Only update availableSlots if explicitly provided, otherwise preserve existing or calculate
        if (request.getAvailableSlots() != null) {
            tour.setAvailableSlots(request.getAvailableSlots());
        }
        
        tour.setStatus(request.getStatus());

        // Map Single Accommodation
        if (request.getAccommodationIds() != null && !request.getAccommodationIds().isEmpty()) {
            Long accId = request.getAccommodationIds().iterator().next();
            Accommodation accommodation = accommodationRepository.findById(accId)
                    .orElseThrow(() -> new ResourceNotFoundException("Accommodation not found with id: " + accId));
            tour.setAccommodation(accommodation);
        }

        // Map Utilities
        if (request.getUtilityIds() != null) {
            Set<Utility> utilities = new HashSet<>(utilityRepository.findAllById(request.getUtilityIds()));
            tour.setUtilities(utilities);
        }

        Tour updatedTour = tourRepository.save(tour);
        return tourMapper.toResponse(updatedTour);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"tourOptions", "popularDestinations"}, allEntries = true)
    public void deleteTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        
        // Soft delete to preserve booking history
        tour.setStatus("DELETED");
        tourRepository.save(tour);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableSlots(Long id, java.time.LocalDate date) {
        if (date == null) return 0;
        Tour tour = tourRepository.findById(id).orElse(null);
        if (tour == null) return 0;
        return tourScheduleRepository.findByTourIdAndDepartureDate(id, date)
                .map(myproject.booking_tour.entity.TourSchedule::getAvailableSlots)
                .orElse(tour.getAvailableSlots() != null ? tour.getAvailableSlots() : (tour.getMaxPeople() != null ? tour.getMaxPeople() : 0));
    }

    @Override
    @org.springframework.cache.annotation.Cacheable("tourOptions")
    public myproject.booking_tour.dto.response.TourOptionsResponse getTourOptions() {
        return new myproject.booking_tour.dto.response.TourOptionsResponse(
            tourRepository.findDistinctDestinations(),
            tourRepository.findDistinctTourTypes(),
            tourRepository.findDistinctTransports()
        );
    }
}
