package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.AccommodationRequest;
import myproject.booking_tour.dto.response.AccommodationResponse;
import myproject.booking_tour.entity.Accommodation;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.AccommodationMapper;
import myproject.booking_tour.repository.AccommodationRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceImplTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationMapper accommodationMapper;

    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    private Accommodation mockAccommodation;
    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        mockAccommodation = new Accommodation();
        mockAccommodation.setId(1L);
        mockAccommodation.setName("Hotel A");
        mockAccommodation.setIsActive(true);
        
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    void getAllAccommodations_ShouldReturnAll_WhenAdmin() {
        mockedSecurityUtil.when(SecurityUtil::isAdmin).thenReturn(true);
        when(accommodationRepository.findAll()).thenReturn(List.of(mockAccommodation));
        when(accommodationMapper.toResponse(any())).thenReturn(new AccommodationResponse());

        List<AccommodationResponse> result = accommodationService.getAllAccommodations();
        assertEquals(1, result.size());
    }

    @Test
    void getAccommodationById_ShouldReturnAccommodation() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(mockAccommodation));
        when(accommodationMapper.toResponse(mockAccommodation)).thenReturn(new AccommodationResponse());

        AccommodationResponse result = accommodationService.getAccommodationById(1L);
        assertNotNull(result);
    }

    @Test
    void createAccommodation_ShouldSave() {
        AccommodationRequest request = new AccommodationRequest();
        when(accommodationMapper.toEntity(request)).thenReturn(mockAccommodation);
        when(accommodationRepository.save(mockAccommodation)).thenReturn(mockAccommodation);
        when(accommodationMapper.toResponse(mockAccommodation)).thenReturn(new AccommodationResponse());

        AccommodationResponse result = accommodationService.createAccommodation(request);

        assertNotNull(result);
        verify(accommodationRepository, times(1)).save(mockAccommodation);
    }

    @Test
    void updateAccommodation_ShouldUpdateAndDeactivateTours_WhenSetToInactive() {
        AccommodationRequest request = new AccommodationRequest();
        request.setIsActive(false);

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(mockAccommodation));
        
        Tour mockTour = new Tour();
        mockTour.setId(10L);
        mockTour.setStatus("ACTIVE");
        when(tourRepository.findByAccommodations_Id(1L)).thenReturn(List.of(mockTour));
        
        when(accommodationRepository.save(any(Accommodation.class))).thenReturn(mockAccommodation);
        when(accommodationMapper.toResponse(any())).thenReturn(new AccommodationResponse());

        AccommodationResponse result = accommodationService.updateAccommodation(1L, request);

        assertNotNull(result);
        assertEquals("INACTIVE", mockTour.getStatus());
        verify(tourRepository, times(1)).saveAll(anyList());
    }
    
    @Test
    void deleteAccommodation_ShouldThrowException_WhenUsedInTour() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(mockAccommodation));
        when(tourRepository.existsByAccommodations_Id(1L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> accommodationService.deleteAccommodation(1L));
        verify(accommodationRepository, never()).delete(any());
    }
}
