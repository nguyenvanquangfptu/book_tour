package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.TourRequest;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.mapper.TourMapper;
import myproject.booking_tour.repository.AccommodationRepository;
import myproject.booking_tour.repository.AuditLogRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.TourScheduleRepository;
import myproject.booking_tour.repository.UtilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceImplTest {

    @Mock
    private TourRepository tourRepository;
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private UtilityRepository utilityRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private TourScheduleRepository tourScheduleRepository;
    @Mock
    private TourMapper tourMapper;

    @InjectMocks
    private TourServiceImpl tourService;

    private Tour mockTour;

    @BeforeEach
    void setUp() {
        mockTour = new Tour();
        mockTour.setId(1L);
        mockTour.setTitle("Test Tour");
        mockTour.setStatus("ACTIVE");
    }

    @Test
    void getTourById_ShouldReturnTour() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
        when(tourMapper.toResponse(mockTour)).thenReturn(new TourResponse());

        TourResponse response = tourService.getTourById(1L);
        assertNotNull(response);
    }

    @Test
    void createTour_ShouldSaveTour() {
        TourRequest request = new TourRequest();
        request.setTitle("New Tour");
        request.setStatus("INACTIVE");

        when(tourMapper.toEntity(request)).thenReturn(new Tour());
        when(tourRepository.existsBySlug(any())).thenReturn(false);
        when(tourRepository.save(any(Tour.class))).thenReturn(mockTour);
        when(tourMapper.toResponse(any(Tour.class))).thenReturn(new TourResponse());

        TourResponse response = tourService.createTour(request);

        assertNotNull(response);
        verify(tourRepository, times(1)).save(any(Tour.class));
    }
}
