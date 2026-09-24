package myproject.booking_tour.service;

import myproject.booking_tour.dto.response.DashboardStatsResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private static Booking booking(String status, long price) {
        Tour tour = new Tour();
        tour.setId(1L);
        tour.setTitle("Ha Long");

        Booking booking = new Booking();
        booking.setStatus(status);
        booking.setTotalPrice(BigDecimal.valueOf(price));
        booking.setBookingDate(LocalDateTime.of(2026, 9, 1, 10, 0));
        booking.setTour(tour);
        return booking;
    }

    /**
     * Don CONFIRMED moi chi duoc duyet, khach chua tra tien va co the bi huy tu
     * dong sau 24 gio - khong phai doanh thu.
     */
    @Test
    void revenue_ShouldCountOnlyPaidBookings() {
        when(bookingRepository.findAll()).thenReturn(List.of(
                booking("PAID", 1000),
                booking("CONFIRMED", 5000),
                booking("PENDING", 7000),
                booking("CANCELLED", 9000)));

        DashboardStatsResponse stats = dashboardService.getDashboardStats();

        assertEquals(0, BigDecimal.valueOf(1000).compareTo(stats.getTotalRevenue()));
        assertEquals(1, stats.getRevenueByMonth().size());
        assertEquals(0, BigDecimal.valueOf(1000).compareTo(stats.getRevenueByMonth().get(0).getRevenue()));
        assertEquals(1, stats.getTopTours().get(0).getTotalBookings());
    }

    @Test
    void chart_ShouldBeEmpty_NotFabricated_WhenThereIsNoRevenue() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking("PENDING", 7000)));

        DashboardStatsResponse stats = dashboardService.getDashboardStats();

        assertEquals(0, BigDecimal.ZERO.compareTo(stats.getTotalRevenue()));
        assertTrue(stats.getRevenueByMonth().isEmpty(), stats.getRevenueByMonth().toString());
        assertTrue(stats.getTopTours().isEmpty());
    }
}
