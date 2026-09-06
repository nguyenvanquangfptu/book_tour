package myproject.booking_tour.dto.response;

import java.time.LocalDateTime;

/**
 * Ket qua cua mot lan dang nhap / dang ky / lam moi.
 *
 * Hai phan di hai duong khac nhau nen khong the gop chung vao AuthResponse:
 *   - {@code body} tra ve trong JSON, frontend luu access token vao localStorage.
 *   - {@code refreshToken} chi duoc dat vao cookie httpOnly, KHONG BAO GIO xuat
 *     hien trong body. Neu no lot vao JSON thi JavaScript doc duoc, va toan bo
 *     ly do dung cookie httpOnly khong con.
 *
 * Controller la noi duy nhat cham vao refreshToken, de dat cookie.
 */
public record AuthResult(AuthResponse body, String refreshToken, LocalDateTime refreshExpiresAt) {
}
