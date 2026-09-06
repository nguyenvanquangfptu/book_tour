package myproject.booking_tour.exception;

/**
 * Mot refresh token da tieu lai duoc gui len lan nua.
 *
 * Tach rieng khoi UnauthorizedException vi mot ly do ky thuat cu the:
 * RefreshTokenService.rotate() khai bao noRollbackFor CHINH XAC lop nay. Truoc
 * khi nem, no da thu hoi ca family - neu ngoai le lam giao dich quay lui thi
 * viec thu hoi bien mat va ke tan cong van dung tiep duoc. Dung
 * UnauthorizedException chung se vo tinh cho moi loi 401 khac commit theo.
 */
public class TokenReuseException extends UnauthorizedException {
    public TokenReuseException(String message) {
        super(message);
    }
}
