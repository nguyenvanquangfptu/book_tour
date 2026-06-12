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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private String month;
        private BigDecimal revenue;
    }
}
