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
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("customerName", request.getFullName());
            templateModel.put("subject", request.getSubject());
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
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact message not found"));
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
