package myproject.booking_tour.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private BigDecimal totalRevenue;
    private long totalBookings;
    private long totalTours;
    private long totalUsers;
    private List<MonthlyRevenue> revenueByMonth;
    private List<TopTour> topTours;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private String month;
        private BigDecimal revenue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopTour {
        private Long tourId;
        private String tourTitle;
        private long totalBookings;
        private BigDecimal revenue;
    }
}
