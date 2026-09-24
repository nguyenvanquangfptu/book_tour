package myproject.booking_tour.utils;

/**
 * Kep cac tham so phan trang den tu query string.
 *
 * VI SAO CAN: page va size di thang tu URL vao PageRequest.of(). Khong kep thi
 *
 *   ?size=1000000  -> co nap mot trieu dong vao bo nho
 *   ?size=-1       -> IllegalArgumentException -> 500 kem stack trace
 *   ?page=-1       -> nhu tren
 *
 * Riêng /api/tours/search la endpoint CONG KHAI, khong can dang nhap, nen hai
 * dong dau la thu bat cu ai cung goi duoc.
 *
 * Tran 100 la con so tuy chon nhung co chu dich: du rong cho moi man hinh that
 * cua ung dung (trang tour dung 10, trang quan tri dung 10), du hep de mot
 * request khong keo sap dong.
 */
public final class PageableUtils {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private PageableUtils() {
    }

    /** Trang am -> 0. */
    public static int safePage(int page) {
        return Math.max(page, 0);
    }

    /** Size <= 0 -> mac dinh; size qua lon -> tran. */
    public static int safeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
