package myproject.booking_tour.service.impl;

import jakarta.mail.MessagingException;
import myproject.booking_tour.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final SpringTemplateEngine templateEngine;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.sender.email:}")
    private String senderEmail;

    @Value("${brevo.sender.name:Booking Tour}")
    private String senderName;

    @Async("taskExecutor")
    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        sendEmailViaBrevo(to, subject, text, false);
    }

    @Async("taskExecutor")
    @Override
    public void sendMessageUsingThymeleafTemplate(String to, String subject, String templateName, Map<String, Object> templateModel) throws MessagingException {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        
        String htmlBody = templateEngine.process("email/" + templateName, thymeleafContext);
        
        sendEmailViaBrevo(to, subject, htmlBody, true);
    }

    private void sendEmailViaBrevo(String to, String subject, String content, boolean isHtml) {
        if (brevoApiKey == null || brevoApiKey.trim().isEmpty()) {
            log.error("Brevo API Key is missing. Email will not be sent.");
            return;
        }

        try {
            String url = "https://api.brevo.com/v3/smtp/email";

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            Map<String, Object> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);

            Map<String, Object> toRecipient = new HashMap<>();
            toRecipient.put("email", to);
            List<Map<String, Object>> toList = new ArrayList<>();
            toList.add(toRecipient);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", sender);
            body.put("to", toList);
            body.put("subject", subject);
            
            if (isHtml) {
                body.put("htmlContent", content);
            } else {
                body.put("textContent", content);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            log.info("Brevo email sent successfully: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to send email via Brevo: " + e.getMessage(), e);
        }
    }
}
