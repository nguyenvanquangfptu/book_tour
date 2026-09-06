package myproject.booking_tour.service;

import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayOSReconciliationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingService bookingService;

    @InjectMocks
    private PayOSReconciliationService reconciliationService;

    private Payment payment;
    private Booking booking;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(7L);

        booking = new Booking();
        booking.setId(42L);
        booking.setUser(user);
        booking.setStatus("CONFIRMED");

        payment = new Payment();
        payment.setId(1L);
        payment.setOrderCode("1001");
        payment.setPaymentStatus("PENDING");
        payment.setBooking(booking);
    }

    @Test
    void shouldMarkPaid_WhenPayOSSaysPaid() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertTrue(reconciliationService.applyStatus(1L, "PAID"));

        assertEquals("PAID", booking.getStatus());
        assertEquals("SUCCESS", payment.getPaymentStatus());
        verify(bookingRepository).save(booking);
        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldCancelBooking_WhenPayOSSaysCancelled() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertTrue(reconciliationService.applyStatus(1L, "CANCELLED"));

        // cancelBooking la noi hoan lai so cho trong va luot dung voucher.
        verify(bookingService).cancelBooking(42L, 7L);
        assertEquals("FAILED", payment.getPaymentStatus());
    }

    @Test
    void shouldCancelBooking_WhenPayOSSaysExpired() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertTrue(reconciliationService.applyStatus(1L, "EXPIRED"));

        verify(bookingService).cancelBooking(42L, 7L);
        assertEquals("FAILED", payment.getPaymentStatus());
    }

    /**
     * Webhook va endpoint /verify cung chay song song voi cong viec dinh ky nay.
     * Giua luc scheduler liet ke danh sach va luc ghi, mot trong hai duong kia
     * co the da xu ly xong - luc do khong duoc dung vao nua.
     */
    @Test
    void shouldDoNothing_WhenPaymentNoLongerPending() {
        payment.setPaymentStatus("SUCCESS");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertFalse(reconciliationService.applyStatus(1L, "PAID"));

        verify(paymentRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(bookingService);
    }

    @Test
    void shouldNotCancelTwice_WhenBookingAlreadyCancelled() {
        booking.setStatus("CANCELLED");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertTrue(reconciliationService.applyStatus(1L, "CANCELLED"));

        // Khong goi lai cancelBooking - no se nem loi va hoan cho trong lan hai.
        verifyNoInteractions(bookingService);
        assertEquals("FAILED", payment.getPaymentStatus());
    }

    @Test
    void shouldLeaveAlone_WhenPayOSStillPending() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertFalse(reconciliationService.applyStatus(1L, "PENDING"));

        assertEquals("PENDING", payment.getPaymentStatus());
        assertEquals("CONFIRMED", booking.getStatus());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldDoNothing_WhenPaymentMissing() {
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertFalse(reconciliationService.applyStatus(99L, "PAID"));

        verifyNoInteractions(bookingService, bookingRepository);
    }
}
