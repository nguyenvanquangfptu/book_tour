package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.ContactMessageRequest;
import myproject.booking_tour.dto.response.ContactMessageResponse;
import myproject.booking_tour.entity.ContactMessage;
import myproject.booking_tour.repository.ContactMessageRepository;
import myproject.booking_tour.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactMessageServiceImplTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ContactMessageServiceImpl contactMessageService;

    private ContactMessage mockMessage;

    @BeforeEach
    void setUp() {
        mockMessage = ContactMessage.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .subject("Support needed")
                .message("I have a question.")
                .status("NEW")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createMessage_ShouldSaveAndSendEmail() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setSubject("Support needed");
        request.setMessage("I have a question.");

        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(mockMessage);

        ContactMessageResponse response = contactMessageService.createMessage(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("NEW", response.getStatus());

        verify(contactMessageRepository, times(1)).save(any(ContactMessage.class));
        verify(emailService, times(1)).sendMessageUsingThymeleafTemplate(
                eq("john@example.com"),
                eq("Cảm ơn bạn đã liên hệ Booking Tour"),
                eq("contact-reply"),
                anyMap()
        );
    }

    /**
     * Form cong khai, dia chi nhan do nguoi gui tu dien. Chu nao nguoi gui go ma
     * lot vao thu tu dong thi form thanh cong cu gui thu lua dao duoi ten Booking
     * Tour toi dia chi bat ky.
     */
    @Test
    void createMessage_ShouldNotEchoAnythingTheSenderTypedIntoTheAutoReply() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest();
        request.setFullName("Nhan qua tai http://lua-dao.example");
        request.setEmail("nan-nhan@example.com");
        request.setSubject("Tai khoan cua ban bi khoa, bam http://lua-dao.example");
        request.setMessage("...");
        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(mockMessage);

        contactMessageService.createMessage(request);

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<java.util.Map<String, Object>> model =
                org.mockito.ArgumentCaptor.forClass(java.util.Map.class);
        verify(emailService).sendMessageUsingThymeleafTemplate(
                eq("nan-nhan@example.com"), anyString(), eq("contact-reply"), model.capture());
        assertTrue(model.getValue().values().stream()
                        .noneMatch(v -> String.valueOf(v).contains("lua-dao")),
                model.getValue().toString());
    }

    @Test
    void createMessage_ShouldSaveEvenIfEmailFails() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setSubject("Support needed");
        request.setMessage("I have a question.");

        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(mockMessage);
        
        // Mock email throwing exception
        doThrow(new RuntimeException("Email sending failed"))
            .when(emailService).sendMessageUsingThymeleafTemplate(anyString(), anyString(), anyString(), anyMap());

        ContactMessageResponse response = contactMessageService.createMessage(request);

        assertNotNull(response);
        verify(contactMessageRepository, times(1)).save(any(ContactMessage.class));
    }

    @Test
    void getAllMessages_ShouldReturnList() {
        when(contactMessageRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(mockMessage));

        List<ContactMessageResponse> responses = contactMessageService.getAllMessages();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        verify(contactMessageRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void updateMessageStatus_ShouldUpdate_WhenFound() {
        when(contactMessageRepository.findById(1L)).thenReturn(Optional.of(mockMessage));
        
        ContactMessage updatedMessage = ContactMessage.builder()
                .id(1L).fullName("John Doe").status("RESOLVED").build();
        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(updatedMessage);

        ContactMessageResponse response = contactMessageService.updateMessageStatus(1L, "RESOLVED");

        assertNotNull(response);
        assertEquals("RESOLVED", response.getStatus());
        verify(contactMessageRepository, times(1)).save(mockMessage);
    }

    @Test
    void updateMessageStatus_ShouldThrowException_WhenNotFound() {
        when(contactMessageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> contactMessageService.updateMessageStatus(1L, "RESOLVED"));
        verify(contactMessageRepository, never()).save(any());
    }
}
