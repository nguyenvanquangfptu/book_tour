package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.UtilityRequest;
import myproject.booking_tour.dto.response.UtilityResponse;
import myproject.booking_tour.entity.Utility;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.UtilityMapper;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.UtilityRepository;
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
class UtilityServiceImplTest {

    @Mock
    private UtilityRepository utilityRepository;

    @Mock
    private UtilityMapper utilityMapper;

    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private UtilityServiceImpl utilityService;

    private Utility mockUtility;
    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        mockUtility = new Utility();
        mockUtility.setId(1L);
        mockUtility.setName("Wifi");
        mockUtility.setIsActive(true);
        
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    void getAllUtilities_ShouldReturnAll_WhenUserIsAdmin() {
        mockedSecurityUtil.when(SecurityUtil::isAdmin).thenReturn(true);
        
        Utility inactiveUtility = new Utility();
        inactiveUtility.setId(2L);
        inactiveUtility.setIsActive(false);

        when(utilityRepository.findAll()).thenReturn(List.of(mockUtility, inactiveUtility));
        when(utilityMapper.toResponse(any(Utility.class))).thenReturn(new UtilityResponse());

        List<UtilityResponse> result = utilityService.getAllUtilities();

        assertEquals(2, result.size());
    }
    
    @Test
    void getAllUtilities_ShouldReturnOnlyActive_WhenUserIsNotAdmin() {
        mockedSecurityUtil.when(SecurityUtil::isAdmin).thenReturn(false);
        
        Utility inactiveUtility = new Utility();
        inactiveUtility.setId(2L);
        inactiveUtility.setIsActive(false);

        when(utilityRepository.findAll()).thenReturn(List.of(mockUtility, inactiveUtility));
        when(utilityMapper.toResponse(any(Utility.class))).thenReturn(new UtilityResponse());

        List<UtilityResponse> result = utilityService.getAllUtilities();

        assertEquals(1, result.size());
    }

    @Test
    void getUtilityById_ShouldReturnUtility_WhenExists() {
        when(utilityRepository.findById(1L)).thenReturn(Optional.of(mockUtility));
        when(utilityMapper.toResponse(mockUtility)).thenReturn(new UtilityResponse());

        UtilityResponse result = utilityService.getUtilityById(1L);

        assertNotNull(result);
    }

    @Test
    void createUtility_ShouldSaveAndReturn() {
        UtilityRequest request = new UtilityRequest();
        when(utilityMapper.toEntity(request)).thenReturn(mockUtility);
        when(utilityRepository.save(mockUtility)).thenReturn(mockUtility);
        when(utilityMapper.toResponse(mockUtility)).thenReturn(new UtilityResponse());

        UtilityResponse result = utilityService.createUtility(request);

        assertNotNull(result);
        verify(utilityRepository, times(1)).save(mockUtility);
    }

    @Test
    void updateUtility_ShouldUpdate_WhenExists() {
        UtilityRequest request = new UtilityRequest();
        request.setName("New Wifi");
        
        when(utilityRepository.findById(1L)).thenReturn(Optional.of(mockUtility));
        when(utilityRepository.save(any(Utility.class))).thenReturn(mockUtility);
        when(utilityMapper.toResponse(mockUtility)).thenReturn(new UtilityResponse());

        UtilityResponse result = utilityService.updateUtility(1L, request);

        assertNotNull(result);
        assertEquals("New Wifi", mockUtility.getName());
    }

    @Test
    void deleteUtility_ShouldDelete_WhenNotUsedInTour() {
        when(utilityRepository.findById(1L)).thenReturn(Optional.of(mockUtility));
        when(tourRepository.existsByUtilityId(1L)).thenReturn(false);

        utilityService.deleteUtility(1L);

        verify(utilityRepository, times(1)).delete(mockUtility);
    }
    
    @Test
    void deleteUtility_ShouldThrowException_WhenUsedInTour() {
        when(utilityRepository.findById(1L)).thenReturn(Optional.of(mockUtility));
        when(tourRepository.existsByUtilityId(1L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> utilityService.deleteUtility(1L));
        verify(utilityRepository, never()).delete(any());
    }
}
