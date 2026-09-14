package myproject.booking_tour.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doc so ngay tour keo dai tu chuoi mo ta tu do ("3 ngay 2 dem", "1 tuan").
 *
 * Con so nay quyet dinh mot booking chiem cho cua bao nhieu ngay, nen MOI noi
 * dung toi no phai doc giong nhau. Truoc day co hai ban: BookingServiceImpl
 * (tru cho, hoan cho) va TourServiceImpl (bao so cho trong cho trang chi tiet
 * tour). Ban thu hai khong biet "tuan" va khong biet "ngay" viet khong dau, nen
 * voi tour "1 tuan" trang chi tiet chi xem cho cua ngay khoi hanh va bao con du
 * cho, trong khi luc dat that thi tru ca 7 ngay va bao het cho.
 */
public final class TourDurationUtils {

    private static final Pattern DAYS = Pattern.compile("(\\d+)\\s*(ngày|ngay|day)", Pattern.CASE_INSENSITIVE);
    private static final Pattern WEEKS = Pattern.compile("(\\d+)\\s*(tuần|tuan|week)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ANY_NUMBER = Pattern.compile("(\\d+)");

    private TourDurationUtils() {
    }

    /** So ngay tour dien ra, toi thieu 1. */
    public static int parseDays(String duration) {
        if (duration == null || duration.trim().isEmpty()) return 1;

        Matcher m = DAYS.matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }

        m = WEEKS.matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1)) * 7;
        }

        m = ANY_NUMBER.matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 1;
    }
}
