package myproject.booking_tour.service.impl;

import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.ContactMessageRequest;
import myproject.booking_tour.dto.response.ContactMessageResponse;
import myproject.booking_tour.entity.ContactMessage;
import myproject.booking_tour.repository.ContactMessageRepository;
import myproject.booking_tour.service.ContactMessageService;
import org.springframework.stereotype.Service;
import myproject.booking_tour.service.EmailService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;

    @Override
    public ContactMessageResponse createMessage(ContactMessageRequest request) {
        ContactMessage message = ContactMessage.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status("NEW")
                .build();
        
        ContactMessage savedMessage = contactMessageRepository.save(message);
        
        try {
            // Thu tu dong KHONG mang lai bat cu chu nao nguoi gui tu go.
            //
            // POST /api/contacts la endpoint cong khai, khong dang nhap, va dia
            // chi nhan la do chinh nguoi gui dien - khong ai xac minh do la hop thu
            // cua ho. Truoc day ho ten va chu de duoc chen nguyen van vao thu: dien
            // email cua nguoi khac, dat "chu de" la mot loi moi kem duong link lua
            // dao, va he thong gui no di duoi ten mien va thuong hieu Booking Tour
            // - vuot qua bo loc thu rac ma ke gian khong the tu vuot.
            Map<String, Object> templateModel = new HashMap<>();
            emailService.sendMessageUsingThymeleafTemplate(request.getEmail(), "Cảm ơn bạn đã liên hệ Booking Tour", "contact-reply", templateModel);
        } catch (Exception e) {
            System.err.println("Failed to send contact reply email: " + e.getMessage());
        }

        return mapToResponse(savedMessage);
    }

    @Override
    public List<ContactMessageResponse> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ContactMessageResponse updateMessageStatus(Long id, String status) {
        // Id không tồn tại là 404, không phải 500.
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new myproject.booking_tour.exception.ResourceNotFoundException(
                        "Contact message not found with id: " + id));
        message.setStatus(status);
        return mapToResponse(contactMessageRepository.save(message));
    }

    private ContactMessageResponse mapToResponse(ContactMessage message) {
        return ContactMessageResponse.builder()
                .id(message.getId())
                .fullName(message.getFullName())
                .email(message.getEmail())
                .subject(message.getSubject())
                .message(message.getMessage())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
