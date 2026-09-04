package myproject.booking_tour.exception;

/**
 * Loi VINH VIEN khi xu ly webhook: chu ky khong hop le, payload sai dinh dang,
 * hoac du lieu khong the anh xa duoc.
 *
 * Phan biet voi loi TAM THOI (mat ket noi DB, Redis timeout...): loi vinh vien
 * gui lai bao nhieu lan cung that bai, nen controller tra 2xx de cong thanh toan
 * NGUNG retry; loi tam thoi tra 5xx de cong thanh toan gui lai.
 */
public class WebhookVerificationException extends RuntimeException {

    public WebhookVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebhookVerificationException(String message) {
        super(message);
    }
}
