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
import myproject.booking_tour.repository.UtilityRepository;
import myproject.booking_tour.repository.AuditLogRepository;
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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final AccommodationRepository accommodationRepository;
    private final UtilityRepository utilityRepository;
    private final AuditLogRepository auditLogRepository;
    private final myproject.booking_tour.repository.TourScheduleRepository tourScheduleRepository;
    private final myproject.booking_tour.repository.BookingRepository bookingRepository;
    private final TourMapper tourMapper;

    @Override
    @org.springframework.cache.annotation.Cacheable("popularDestinations")
    public List<PopularDestinationResponse> getPopularDestinations(int limit) {
        return tourRepository.findPopularDestinations(PageRequest.of(0, limit));
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
    public PageResponse<TourResponse> searchAndFilterTours(String keyword, String destination, Integer durationDays, Integer guests, BigDecimal minPrice, BigDecimal maxPrice, String status, List<String> tourTypes, List<String> transports, int page, int size, String sortBy, String sortDir) {
        if (!myproject.booking_tour.security.SecurityUtil.isAdmin()) {
            status = "ACTIVE";
        }
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        org.springframework.data.jpa.domain.Specification<Tour> spec = myproject.booking_tour.repository.specification.TourSpecification.filterTours(keyword, destination, durationDays, guests, minPrice, maxPrice, status, tourTypes, transports);
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

        // Map Accommodations from set of IDs in Request
        if (request.getAccommodationIds() != null && !request.getAccommodationIds().isEmpty()) {
            java.util.List<Accommodation> accommodations = accommodationRepository.findAllById(request.getAccommodationIds());
            if (accommodations.size() != request.getAccommodationIds().size()) {
                throw new ResourceNotFoundException("One or more Accommodations not found");
            }
            tour.setAccommodations(new java.util.HashSet<>(accommodations));
        }

        // Map Utilities from set in Request
        if (request.getUtilityIds() != null && !request.getUtilityIds().isEmpty()) {
            java.util.List<Utility> utilities = utilityRepository.findAllById(request.getUtilityIds());
            tour.setUtilities(utilities);
        }

        if (tour.getAvailableSlots() == null) {
            tour.setAvailableSlots(tour.getMaxPeople() != null ? tour.getMaxPeople() : 0);
        }

        if (tour.getStatus() == null || tour.getStatus().trim().isEmpty()) {
            tour.setStatus("INACTIVE");
        }

        if ("ACTIVE".equals(tour.getStatus())) {
            boolean hasInactiveAcc = tour.getAccommodations().stream()
                    .anyMatch(acc -> Boolean.FALSE.equals(acc.getIsActive()));
            if (hasInactiveAcc) {
                throw new RuntimeException("Không thể mở bán Tour: Tồn tại Nơi lưu trú đang ngưng hoạt động.");
            }
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
        
        if (tour.getPrice() != null && request.getPrice() != null && tour.getPrice().compareTo(request.getPrice()) != 0) {
            myproject.booking_tour.entity.AuditLog log = new myproject.booking_tour.entity.AuditLog();
            log.setEntityName("Tour");
            log.setEntityId(tour.getId());
            log.setAction("UPDATE_PRICE");
            log.setOldValue(tour.getPrice().toString());
            log.setNewValue(request.getPrice().toString());
            // Assuming system action since we don't have current user context here
            log.setUserId(0L); 
            auditLogRepository.save(log);
        }
        
        tour.setPrice(request.getPrice());
        tour.setDuration(request.getDuration());
        tour.setImageUrl(request.getImageUrl());
        tour.setImages(request.getImages());

        tour.setMaxPeople(request.getMaxPeople());
        tour.setTourType(request.getTourType());
        tour.setTransport(request.getTransport());
        tour.setHighlights(request.getHighlights());
        
        if (request.getItinerary() != null) {
            tour.getItinerary().clear();
            List<myproject.booking_tour.entity.TourItinerary> newItinerary = request.getItinerary().stream().map(dto -> {
                myproject.booking_tour.entity.TourItinerary item = new myproject.booking_tour.entity.TourItinerary();
                item.setDay(dto.getDay());
                item.setTitle(dto.getTitle());
                item.setDescription(dto.getDescription());
                return item;
            }).collect(Collectors.toList());
            tour.getItinerary().addAll(newItinerary);
        }

        // Only update availableSlots if explicitly provided, otherwise preserve existing or calculate
        if (request.getAvailableSlots() != null) {
            tour.setAvailableSlots(request.getAvailableSlots());
        }
        
        tour.setStatus(request.getStatus());

        // Map Accommodations
        if (request.getAccommodationIds() != null && !request.getAccommodationIds().isEmpty()) {
            java.util.List<Accommodation> accommodations = accommodationRepository.findAllById(request.getAccommodationIds());
            if (accommodations.size() != request.getAccommodationIds().size()) {
                throw new ResourceNotFoundException("One or more Accommodations not found");
            }
            tour.setAccommodations(new java.util.HashSet<>(accommodations));
        } else {
            tour.getAccommodations().clear();
        }

        // Map Utilities
        if (request.getUtilityIds() != null) {
            java.util.List<Utility> utilities = utilityRepository.findAllById(request.getUtilityIds());
            tour.setUtilities(utilities);
        }

        Tour updatedTour = tour;

        if ("ACTIVE".equals(updatedTour.getStatus())) {
            boolean hasInactiveAcc = updatedTour.getAccommodations().stream()
                    .anyMatch(acc -> Boolean.FALSE.equals(acc.getIsActive()));
            if (hasInactiveAcc) {
                throw new RuntimeException("Không thể mở bán Tour: Tồn tại Nơi lưu trú đang ngưng hoạt động. Vui lòng thay Nơi lưu trú khác trước khi Active!");
            }
        }

        updatedTour = tourRepository.save(updatedTour);
        return tourMapper.toResponse(updatedTour);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"tourOptions", "popularDestinations"}, allEntries = true)
    public void deleteTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        
        if (bookingRepository.existsByTourId(id)) {
            // Soft delete to preserve booking history
            tour.setStatus("DELETED");
            tourRepository.save(tour);
        } else {
            // Hard delete
            tourScheduleRepository.deleteByTourId(id);
            tourRepository.delete(tour);
        }
    }

    private int parseDurationDays(String duration) {
        if (duration == null || duration.trim().isEmpty()) return 1;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)\\s*(ngày|day)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        p = java.util.regex.Pattern.compile("(\\d+)");
        m = p.matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 1;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableSlots(Long id, java.time.LocalDate date) {
        if (date == null) return 0;
        Tour tour = tourRepository.findById(id).orElse(null);
        if (tour == null) return 0;
        
        int days = parseDurationDays(tour.getDuration());
        int defaultSlots = tour.getAvailableSlots() != null ? tour.getAvailableSlots() : (tour.getMaxPeople() != null ? tour.getMaxPeople() : 0);
        int minAvailable = defaultSlots;

        for (int i = 0; i < days; i++) {
            java.time.LocalDate checkDate = date.plusDays(i);
            int availableOnDate = tourScheduleRepository.findFirstByTourIdAndDepartureDate(id, checkDate)
                    .map(myproject.booking_tour.entity.TourSchedule::getAvailableSlots)
                    .orElse(defaultSlots);
            if (availableOnDate < minAvailable) {
                minAvailable = availableOnDate;
            }
        }
        
        return minAvailable;
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
