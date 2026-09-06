package myproject.booking_tour.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    /**
     * Id cua nguoi dung dang dang nhap, hoac null neu khong co ai (tac vu chay
     * nen: scheduler, webhook cua cong thanh toan).
     * <p>
     * Truoc day cac cho ghi audit log dung gia tri gia userId = 0 vi khong lay
     * duoc user - ma trong bang users khong he co id = 0, khien khong the dat
     * khoa ngoai va nhat ky khong biet ai da thao tac. null moi la cach dung de
     * dien dat "khong ro nguoi thuc hien".
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getId();
        }
        return null;
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}