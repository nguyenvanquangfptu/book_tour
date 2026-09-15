package myproject.booking_tour.service;

import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.response.DashboardStatsResponse;
import myproject.booking_tour.dto.response.DashboardStatsResponse.MonthlyRevenue;
import myproject.booking_tour.dto.response.DashboardStatsResponse.TopTour;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    public DashboardStatsResponse getDashboardStats() {
        long totalTours = tourRepository.count();
        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.count();

        List<Booking> allBookings = bookingRepository.findAll();

        // Doanh thu chi tinh don DA THANH TOAN. Truoc day ca ba cho duoi day
        // cong them don CONFIRMED - don admin vua duyet, khach chua tra dong
        // nao, va phan lon se bi BookingScheduler huy sau 24 gio. Moi lan duyet
        // mot don, "Tong doanh thu" tang len; mot ngay sau don bi huy thi so do
        // lang le tut xuong, va con so tren bang dieu khien khong khop voi tien
        // thuc nhan o PayOS.
        BigDecimal totalRevenue = allBookings.stream()
                .filter(DashboardService::isRevenue)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group revenue by Month (YYYY-MM)
        Map<String, BigDecimal> revenueMap = new TreeMap<>();
        
        for (Booking b : allBookings) {
            if (isRevenue(b) && b.getBookingDate() != null) {
                String month = b.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                revenueMap.put(month, revenueMap.getOrDefault(month, BigDecimal.ZERO).add(b.getTotalPrice()));
            }
        }

        List<MonthlyRevenue> monthlyRevenues = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : revenueMap.entrySet()) {
            monthlyRevenues.add(new MonthlyRevenue(entry.getKey(), entry.getValue()));
        }

        // Khong co doanh thu thi bieu do trong. Truoc day cho nay nhet vao sau
        // thang doanh thu bia (15-40 trieu/thang) "cho bieu do dep": mot he
        // thong chua ban duoc ve nao hien ra bang dieu khien co doanh thu that
        // su, trong khi o "Tong doanh thu" ngay ben tren van la 0.

        // Compute Top Tours
        Map<Long, TopTour> tourStatsMap = new HashMap<>();
        for (Booking b : allBookings) {
            if (isRevenue(b) && b.getTour() != null) {
                Long tId = b.getTour().getId();
                TopTour topTour = tourStatsMap.getOrDefault(tId, new TopTour(tId, b.getTour().getTitle(), 0, BigDecimal.ZERO));
                topTour.setTotalBookings(topTour.getTotalBookings() + 1);
                topTour.setRevenue(topTour.getRevenue().add(b.getTotalPrice()));
                tourStatsMap.put(tId, topTour);
            }
        }
        List<TopTour> topTours = tourStatsMap.values().stream()
                .sorted((t1, t2) -> t2.getRevenue().compareTo(t1.getRevenue()))
                .limit(5)
                .collect(Collectors.toList());

        return new DashboardStatsResponse(
                totalRevenue,
                totalBookings,
                totalTours,
                totalUsers,
                monthlyRevenues,
                topTours
        );
    }

    private static boolean isRevenue(Booking booking) {
        return "PAID".equals(booking.getStatus());
    }
}
