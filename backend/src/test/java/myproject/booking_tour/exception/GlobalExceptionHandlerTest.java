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
