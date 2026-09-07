package myproject.booking_tour.scheduler;

import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.service.BookingAutoCancelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tinh chat quan trong nhat cua lop nay: MOT DON HONG CHI LAM HONG CHINH NO.
 *
 * Ban truoc (BookingServiceImpl.autoCancelUnpaidBookings) goi ca vong lap trong
 * mot @Transactional, nen mot don gap su co keo sap ca luot quet. Cac test duoi
 * day khoa chat viec do khong duoc tai dien.
 */
@ExtendWith(MockitoExtension.class)
class BookingSchedulerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingAutoCancelService autoCancelService;

    @InjectMocks
    private BookingScheduler scheduler;

    private Booking confirmed(long id) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus("CONFIRMED");
        return booking;
    }

    private void givenConfirmed(Booking... bookings) {
        when(bookingRepository.findByStatus("CONFIRMED")).thenReturn(List.of(bookings));
    }

    @Test
    void shouldKeepSweeping_WhenOneBookingFails() {
        givenConfirmed(confirmed(1L), confirmed(2L), confirmed(3L));
        when(autoCancelService.cancelIfStillOverdue(eq(1L), any())).thenReturn(true);
        when(autoCancelService.cancelIfStillOverdue(eq(2L), any()))
                .thenThrow(new RuntimeException("chủ tài khoản đã bị xóa"));
        when(autoCancelService.cancelIfStillOverdue(eq(3L), any())).thenReturn(true);

        assertDoesNotThrow(scheduler::cancelUnpaidBookings);

        // Đơn #3 vẫn phải được xử lý dù đơn #2 nổ ngay trước nó.
        verify(autoCancelService).cancelIfStillOverdue(eq(3L), any());
    }

    @Test
    void shouldHandEachBookingToItsOwnTransaction() {
        givenConfirmed(confirmed(1L), confirmed(2L));
        when(autoCancelService.cancelIfStillOverdue(any(), any())).thenReturn(false);

        scheduler.cancelUnpaidBookings();

        // Mỗi đơn một lời gọi qua proxy - đó là thứ mở transaction riêng cho nó.
        verify(autoCancelService).cancelIfStillOverdue(eq(1L), any());
        verify(autoCancelService).cancelIfStillOverdue(eq(2L), any());
        verifyNoMoreInteractions(autoCancelService);
    }

    @Test
    void shouldPassDeadlineOf24HoursAgo() {
        givenConfirmed(confirmed(1L));
        when(autoCancelService.cancelIfStillOverdue(any(), any())).thenReturn(false);

        scheduler.cancelUnpaidBookings();

        ArgumentCaptor<LocalDateTime> deadline = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(autoCancelService).cancelIfStillOverdue(eq(1L), deadline.capture());

        LocalDateTime expected = LocalDateTime.now().minusHours(24);
        assertTrue(deadline.getValue().isAfter(expected.minusMinutes(1)), deadline.getValue().toString());
        assertTrue(deadline.getValue().isBefore(expected.plusMinutes(1)), deadline.getValue().toString());
    }

    @Test
    void shouldDoNothing_WhenNoConfirmedBookings() {
        when(bookingRepository.findByStatus("CONFIRMED")).thenReturn(List.of());

        scheduler.cancelUnpaidBookings();

        verifyNoInteractions(autoCancelService);
    }

    @Test
    void shouldNotLookForTheStatusThatNeverExisted() {
        // Bản cũ của lớp này quét findByStatus("APPROVED") - một trạng thái
        // không đoạn code nào trong dự án đặt, nên nó chạy mỗi phút để trả về
        // danh sách rỗng, vĩnh viễn.
        givenConfirmed(confirmed(1L));
        when(autoCancelService.cancelIfStillOverdue(any(), any())).thenReturn(false);

        scheduler.cancelUnpaidBookings();

        verify(bookingRepository, never()).findByStatus("APPROVED");
    }
}
