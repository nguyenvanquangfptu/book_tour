package myproject.booking_tour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;

/**
 * Hoi PayOS xem mot don hang dang o trang thai nao.
 *
 * Tach rieng ra mot lop nho vi day la RANH GIOI GIUA MANG VA DATABASE. Lenh goi
 * ben trong la mot request HTTP dong bo, co the mat vai tram mili giay hoac
 * timeout - tuyet doi khong duoc chay khi dang mo mot transaction.
 * PayOSReconciliationScheduler goi lop nay TRUOC, roi moi mo transaction de ghi
 * ket qua.
 *
 * Tach ra cung khien tang lich (scheduler) kiem thu duoc ma khong can den SDK
 * that cua PayOS.
 */
@Component
@RequiredArgsConstructor
public class PayOSStatusReader {

    private final PayOS payOS;

    /**
     * Tra ve ten trang thai PayOS dang ghi nhan: PAID, CANCELLED, EXPIRED,
     * PENDING, ...
     *
     * Nem ngoai le neu khong hoi duoc (mang loi, don hang khong ton tai). Ben
     * goi phai tu quyet dinh bo qua hay thu lai.
     */
    public String statusOf(String orderCode) {
        long code = Long.parseLong(orderCode);
        return payOS.paymentRequests().get(code).getStatus().name();
    }
}
