package myproject.booking_tour.service;

import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingAutoCancelServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingAutoCancelService autoCancelService;

    private LocalDateTime deadline;
    private Booking booking;

    @BeforeEach
    void setUp() {
        deadline = LocalDateTime.now().minusHours(24);

        User owner = new User();
        owner.setId(5L);
        owner.setEmail("khach@test.com");
        owner.setFullName("Khách Test");

        Tour tour = new Tour();
        tour.setTitle("Tour Đà Lạt");

        booking = new Booking();
        booking.setId(42L);
        booking.setUser(owner);
        booking.setTour(tour);
        booking.setStatus("CONFIRMED");
        booking.setApprovedAt(LocalDateTime.now().minusDays(3));

        // Duong huy cua he thong tra ve true khi that su huy duoc. Khai bao
        // lenient vi cac test "khong duoc huy" khong cham toi no.
        lenient().when(bookingService.cancelBookingBySystem(anyLong(), anyString())).thenReturn(true);
    }

    @Test
    void shouldCancelAndNotify_WhenOverdue() throws Exception {
        when(bookingRepository.findById(42L)).thenReturn(Optional.of(booking));

        boolean cancelled = autoCancelService.cancelIfStillOverdue(42L, deadline);

        assertTrue(cancelled);
        verify(bookingService).cancelBookingBySystem(eq(42L), anyString());
        verify(emailService).sendMessageUsingThymeleafTemplate(
                eq("khach@test.com"), anyString(), eq("booking-cancelled-auto"), anyMap());
    }

    @Test
    void shouldNotCancel_WhenBookingWasPaidBetweenListingAndProcessing() {
        // Đây là lý do phải đọc lại trạng thái BÊN TRONG transaction: giữa lúc
        // scheduler liệt kê danh sách và lúc chạy tới đây, webhook PayOS có thể
        // đã chuyển đơn sang PAID. Bản cũ không kiểm tra lại và sẽ hủy thẳng một
        // đơn vừa được trả tiền xong.
        booking.setStatus("PAID");
        when(bookingRepository.findById(42L)).thenReturn(Optional.of(booking));

        boolean cancelled = autoCancelService.cancelIfStillOverdue(42L, deadline);

        assertFalse(cancelled);
        verify(bookingService, never()).cancelBookingBySystem(any(), any());
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldNotCancel_WhenStillInsideGracePeriod() {
        booking.setApprovedAt(LocalDateTime.now().minusHours(1));
        when(bookingRepository.findById(42L)).thenReturn(Optional.of(booking));

        boolean cancelled = autoCancelService.cancelIfStillOverdue(42L, deadline);

        assertFalse(cancelled);
        verify(bookingService, never()).cancelBookingBySystem(any(), any());
    }

    @Test
    void shouldFallBackToBookingDate_WhenApprovedAtIsMissing() {
        booking.setApprovedAt(null);
        booking.setBookingDate(LocalDateTime.now().minusDays(3));
        when(bookingRepository.findById(42L)).thenReturn(Optional.of(booking));

        assertTrue(autoCancelService.cancelIfStillOverdue(42L, deadline));
        verify(bookingService).cancelBookingBySystem(eq(42L), anyString());
    }

    @Test
    void shouldDoNothing_WhenBookingDisappeared() {
        when(bookingRepository.findById(42L)).thenReturn(Optional.empty());

        assertFalse(autoCancelService.cancelIfStillOverdue(42L, deadline));
        verifyNoInteractions(bookingService);
    }

    @Test
    void shouldStillReportCancelled_WhenEmailFails() throws Exception {
        // Đơn đã hủy xong rồi; gửi mail hỏng không được kéo cả transaction xuống.
        when(bookingRepository.findById(42L)).thenReturn(Optional.of(booking));
        doThrow(new RuntimeException("SMTP down")).when(emailService)
                .sendMessageUsingThymeleafTemplate(anyString(), anyString(), anyString(), anyMap());

        assertTrue(autoCancelService.cancelIfStillOverdue(42L, deadline));
        verify(bookingService).cancelBookingBySystem(eq(42L), anyString());
    }
}
