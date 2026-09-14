package myproject.booking_tour.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TourDurationUtilsTest {

    @Test
    void shouldReadDaysWithOrWithoutDiacritics() {
        assertEquals(3, TourDurationUtils.parseDays("3 ngày 2 đêm"));
        assertEquals(3, TourDurationUtils.parseDays("3 ngay 2 dem"));
        assertEquals(4, TourDurationUtils.parseDays("4 Days 3 Nights"));
    }

    @Test
    void shouldCountAWeekAsSevenDays() {
        assertEquals(7, TourDurationUtils.parseDays("1 tuần"));
        assertEquals(14, TourDurationUtils.parseDays("2 tuan"));
        assertEquals(7, TourDurationUtils.parseDays("1 week"));
    }

    @Test
    void shouldPreferTheDayCountOverTheNightCount() {
        // "2 đêm" đứng trước nhưng số ngày mới là số chỗ phải giữ.
        assertEquals(3, TourDurationUtils.parseDays("2 đêm 3 ngày"));
    }

    @Test
    void shouldFallBackToOneDay() {
        assertEquals(1, TourDurationUtils.parseDays(null));
        assertEquals(1, TourDurationUtils.parseDays("  "));
        assertEquals(1, TourDurationUtils.parseDays("trong ngày"));
        assertEquals(5, TourDurationUtils.parseDays("5"));
    }
}
