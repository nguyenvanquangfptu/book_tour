package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.BookingRequest;
import myproject.booking_tour.dto.response.BookingResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.mapper.BookingMapper;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.TourScheduleRepository;
import myproject.booking_tour.repository.UserRepository;
import myproject.booking_tour.repository.VoucherRepository;
import myproject.booking_tour.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private TourScheduleRepository tourScheduleRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User mockUser;
    private Tour mockTour;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockTour = new Tour();
        mockTour.setId(10L);
        mockTour.setStatus("ACTIVE");
        mockTour.setPrice(BigDecimal.valueOf(100));
        mockTour.setAvailableSlots(10);
    }

    @Test
    void createBooking_ShouldThrowException_WhenTourIsSoldOut() {
        BookingRequest request = new BookingRequest();
        request.setTourId(10L);
        
        mockTour.setStatus("SOLD_OUT");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request, 1L));
    }
    
    @Test
    void createBooking_ShouldThrowException_WhenDateInPast() {
        BookingRequest request = new BookingRequest();
        request.setTourId(10L);
        request.setTravelDate(LocalDate.now().minusDays(1));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request, 1L));
    }
}
