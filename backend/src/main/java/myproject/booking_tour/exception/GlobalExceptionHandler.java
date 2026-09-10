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

    /** SQLSTATE 23505 - unique_violation. */
    private static final String UNIQUE_VIOLATION = "23505";

    /**
     * Vi pham rang buoc toan ven cua database. KHONG phai loai nao cung giong
     * nhau, va phan biet chung moi la diem chinh cua handler nay.
     *
     * UNIQUE (23505) gan nhu luon la hai request dong thoi cung tao mot ban ghi
     * ma rang buoc chi cho phep mot - vi du hai nguoi cung dat mot ngay khoi
     * hanh chua co dong lich. Day la TRANH CHAP: thu lai la thanh cong, nen 409.
     *
     * Moi vi pham con lai - khoa ngoai (23503), NOT NULL (23502), CHECK (23514)
     * - la LOI LAP TRINH: code gui xuong database du lieu ma no khong chap nhan.
     * Thu lai khong bao gio thanh cong.
     *
     * Ban dau tuc handler nay tra 409 cho MOI vi pham, va cai gia phai tra da
     * hien ra ngay: VoucherServiceImpl ghi audit log voi user_id = 0 - mot id
     * khong ton tai trong bang users - nen tao voucher luon that bai voi loi
     * khoa ngoai, con admin thi nhan duoc "Du lieu vua duoc thay doi boi mot
     * giao dich khac. Vui long thu lai!" va bam lai mai khong duoc. Mot loi that
     * bi mac ao tranh chap tam thoi, va khong loi ERROR nao trong log de lan ra.
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String sqlState = cause instanceof java.sql.SQLException sqlException
                ? sqlException.getSQLState()
                : null;

        if (UNIQUE_VIOLATION.equals(sqlState)) {
            log.warn("Tranh chấp ràng buộc UNIQUE: {}", cause.getMessage());
            ApiResponse<?> response = new ApiResponse<>(false,
                    "Dữ liệu vừa được thay đổi bởi một giao dịch khác. Vui lòng thử lại!", null);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        log.error("Vi phạm ràng buộc dữ liệu (SQLSTATE {}): ", sqlState, ex);
        ApiResponse<?> response = new ApiResponse<>(false, "Đã có lỗi xảy ra, vui lòng thử lại sau!", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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
