package myproject.booking_tour.exception;

import myproject.booking_tour.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleResourceNotFoundException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Not found");
    }

    @Test
    void handleBadRequestException_ShouldReturn400() {
        BadRequestException ex = new BadRequestException("Bad request");
        
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleBadRequestException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Bad request");
    }

    @Test
    void handleUnauthorizedException_ShouldReturn401() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized");
        
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleUnauthorizedException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Unauthorized");
    }

    @Test
    void handleOptimisticLockingException_ShouldReturn409() {
        org.springframework.orm.ObjectOptimisticLockingFailureException ex = 
                new org.springframework.orm.ObjectOptimisticLockingFailureException("Entity", new RuntimeException());
        
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleOptimisticLockingException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Vui lòng thử lại");
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturn409_NotServerError() {
        // Hai request cùng tạo một dòng lịch khởi hành: ràng buộc UNIQUE chặn
        // request thứ hai. Đây là tranh chấp (thử lại được), không phải sự cố
        // máy chủ - trước khi có handler này nó rơi vào handleGeneralException
        // và trả 500.
        org.springframework.dao.DataIntegrityViolationException ex =
                new org.springframework.dao.DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"uq_tour_schedules_tour_date\"");

        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Vui lòng thử lại");
        // Chi tiết ràng buộc của database không được lộ ra client
        assertThat(response.getBody().getMessage()).doesNotContain("uq_tour_schedules_tour_date");
    }

    @Test
    void handleMethodNotSupported_ShouldReturn405WithAllowHeader() {
        // GET /api/tours: đường dẫn tồn tại nhưng chỉ map POST. Đây là lỗi của
        // bên gọi, không phải sự cố máy chủ - trước khi có handler này nó rơi
        // vào handleGeneralException và trả 500 kèm nguyên stack trace.
        org.springframework.web.HttpRequestMethodNotSupportedException ex =
                new org.springframework.web.HttpRequestMethodNotSupportedException(
                        "GET", java.util.List.of("POST"));

        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleMethodNotSupported(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("GET");
        // 405 mà thiếu Allow thì bên gọi không biết phải dùng phương thức nào.
        assertThat(response.getHeaders().getAllow())
                .containsExactly(org.springframework.http.HttpMethod.POST);
    }

    @Test
    void handleGeneralException_ShouldReturn500() {
        // Message nội bộ (vd: "General error") không được lộ ra client - chỉ trả thông báo chung chung
        Exception ex = new Exception("General error");

        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleGeneralException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).doesNotContain("General error");
    }
}
