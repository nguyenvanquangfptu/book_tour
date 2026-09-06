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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        log.error("Lỗi hệ thống nghiêm trọng: ", ex);
        ApiResponse<?> response = new ApiResponse<>(false, "Đã có lỗi xảy ra, vui lòng thử lại sau!", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
