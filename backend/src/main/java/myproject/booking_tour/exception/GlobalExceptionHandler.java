package myproject.booking_tour.exception;

import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Tài nguyên không tồn tại: {}", ex.getMessage());
        ApiResponse<?> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequestException(BadRequestException ex) {
        log.warn("Yêu cầu không hợp lệ: {}", ex.getMessage());
        ApiResponse<?> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("Lỗi xác thực/truy cập: {}", ex.getMessage());
        ApiResponse<?> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<?>> handleOptimisticLockingException(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        log.warn("Lỗi conflict dữ liệu (Optimistic Locking): {}", ex.getMessage());
        ApiResponse<?> response = new ApiResponse<>(false, "Hệ thống đang xử lý giao dịch khác, số lượng hoặc voucher đã bị thay đổi. Vui lòng thử lại!", null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Vi pham rang buoc toan ven cua database - gan nhu luon la hai request
     * dong thoi cung tao mot ban ghi ma rang buoc UNIQUE chi cho phep mot
     * (vi du hai nguoi cung dat mot ngay khoi hanh chua co dong lich).
     *
     * Day la TRANH CHAP, khong phai su co may chu: nguoi dung thu lai la thanh
     * cong. Truoc khi co handler nay no roi vao handleGeneralException va thanh
     * 500 - cung mot tinh huong tranh chap ma luc thi 409 "thu lai di", luc thi
     * 500 "may chu hong", tuy vao viec dong du lieu da ton tai hay chua.
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex) {
        log.warn("Vi phạm ràng buộc dữ liệu: {}", ex.getMostSpecificCause().getMessage());
        ApiResponse<?> response = new ApiResponse<>(false,
                "Dữ liệu vừa được thay đổi bởi một giao dịch khác. Vui lòng thử lại!", null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Lỗi validation: {}", errorMessage);
        ApiResponse<?> response = new ApiResponse<>(false, errorMessage, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Tham so trong duong dan hoac query sai kieu - vi du GET /api/payments/abc
     * trong khi {id} phai la so.
     *
     * Day la loi cua BEN GOI, phai tra 400. Truoc khi co handler nay no roi vao
     * handleGeneralException va thanh 500 kem nguyen stack trace ghi o muc ERROR
     * - bat ky URL rac nao cung do day duoc log, va cai 500 khien nguoi doc
     * tuong may chu hong.
     *
     * Truong hop lam lo ra dieu nay: sau khi go bo GET /api/payments/payos-callback,
     * duong dan cu roi vao mapping /api/payments/{id} va Spring co ep chuoi
     * "payos-callback" thanh Long.
     */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        log.warn("Tham so '{}' sai kieu: nhan duoc '{}'", ex.getName(), ex.getValue());
        ApiResponse<?> response = new ApiResponse<>(false,
                "Tham số '" + ex.getName() + "' không hợp lệ.", null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Goi dung duong dan nhung sai phuong thuc HTTP - vi du GET /api/tours,
     * trong khi o duong dan do chi co POST (tao tour); danh sach tour nam o
     * endpoint khac.
     *
     * Cung ly do voi handleTypeMismatch ngay tren: day la loi cua BEN GOI, phai
     * tra 405. Truoc khi co handler nay no roi vao handleGeneralException va
     * thanh 500 kem nguyen stack trace ghi o muc ERROR - nguoi doc log tuong
     * may chu hong, con nguoi goi thi khong biet minh sai o dau.
     *
     * Header "Allow" la bat buoc theo dac ta HTTP cho 405, va no chinh la thu
     * noi cho ben goi biet duong dan nay nhan phuong thuc nao.
     */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        log.warn("Phương thức {} không được hỗ trợ, đường dẫn này chỉ nhận {}",
                ex.getMethod(), ex.getSupportedHttpMethods());

        ApiResponse<?> response = new ApiResponse<>(false,
                "Phương thức " + ex.getMethod() + " không được hỗ trợ cho đường dẫn này.", null);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (ex.getSupportedHttpMethods() != null) {
            headers.setAllow(ex.getSupportedHttpMethods());
        }
        return new ResponseEntity<>(response, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        log.error("Lỗi hệ thống nghiêm trọng: ", ex);
        ApiResponse<?> response = new ApiResponse<>(false, "Đã có lỗi xảy ra, vui lòng thử lại sau!", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
