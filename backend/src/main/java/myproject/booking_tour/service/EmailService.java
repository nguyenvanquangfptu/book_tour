package myproject.booking_tour.service;

import jakarta.mail.MessagingException;
import java.util.Map;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    void sendMessageUsingThymeleafTemplate(String to, String subject, String templateName, Map<String, Object> templateModel) throws MessagingException;
}
