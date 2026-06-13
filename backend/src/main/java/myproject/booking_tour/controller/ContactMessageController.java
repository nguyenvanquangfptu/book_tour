package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.ContactMessageRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.ContactMessageResponse;
import myproject.booking_tour.service.ContactMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactMessageResponse>> createMessage(@Valid @RequestBody ContactMessageRequest request) {
        ContactMessageResponse message = contactMessageService.createMessage(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Gửi tin nhắn liên hệ thành công!", message));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactMessageResponse>>> getAllMessages() {
        List<ContactMessageResponse> messages = contactMessageService.getAllMessages();
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách tin nhắn thành công!", messages));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ContactMessageResponse>> updateStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        ContactMessageResponse message = contactMessageService.updateMessageStatus(id, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật trạng thái thành công!", message));
    }
}
