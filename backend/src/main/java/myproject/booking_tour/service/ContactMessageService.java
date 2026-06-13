package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.ContactMessageRequest;
import myproject.booking_tour.dto.response.ContactMessageResponse;

import java.util.List;

public interface ContactMessageService {
    ContactMessageResponse createMessage(ContactMessageRequest request);
    List<ContactMessageResponse> getAllMessages();
    ContactMessageResponse updateMessageStatus(Long id, String status);
}
