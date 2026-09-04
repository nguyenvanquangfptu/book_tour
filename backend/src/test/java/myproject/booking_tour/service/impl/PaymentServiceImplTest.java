package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.PaymentResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
import myproject.booking_tour.mapper.PaymentMapper;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.PaymentRepository;
import myproject.booking_tour.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.payos.PayOS;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private BookingService bookingService;
    @Mock
    private PayOS payOS;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Booking mockBooking;

    @BeforeEach
    void setUp() {
        mockBooking = new Booking();
        mockBooking.setId(1L);
        mockBooking.setTotalPrice(BigDecimal.valueOf(1000));
    }

    @Test
    void createPayment_ShouldSavePayment() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethod("CASH");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(10L);
            return p;
        });
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(new PaymentResponse());

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}
