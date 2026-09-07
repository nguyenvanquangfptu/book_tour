package myproject.booking_tour.service.impl;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        // By default, do not set brevoApiKey so it doesn't make real HTTP calls
    }

    @Test
    void sendSimpleMessage_ShouldReturnEarly_WhenApiKeyIsMissing() {
        // Act
        emailService.sendSimpleMessage("test@example.com", "Subject", "Text");

        // Assert
        // Since apiKey is null, it should return without throwing exceptions and no HTTP call is made.
        // We just verify it executes without error.
    }

    @Test
    void sendMessageUsingThymeleafTemplate_ShouldProcessTemplate() throws MessagingException {
        // Arrange
        Map<String, Object> model = new HashMap<>();
        model.put("key", "value");

        when(templateEngine.process(eq("email/test-template"), any(Context.class)))
                .thenReturn("<html>Test</html>");

        // Act
        emailService.sendMessageUsingThymeleafTemplate("test@example.com", "Subject", "test-template", model);

        // Assert
        verify(templateEngine, times(1)).process(eq("email/test-template"), any(Context.class));
    }
    
    @Test
    void sendEmailViaBrevo_ShouldHandleException_WhenErrorOccurs() throws MessagingException {
        // We set a fake API key so it tries to execute the REST call
        ReflectionTestUtils.setField(emailService, "brevoApiKey", "fake-key");
        
        // When it tries to make the REST call to a fake/real endpoint, it might throw an exception 
        // depending on the environment. The method catches Exception and logs it.
        // We just ensure it doesn't throw the exception up.
        
        Map<String, Object> model = new HashMap<>();
        when(templateEngine.process(eq("email/test-template"), any(Context.class)))
                .thenReturn("<html>Test</html>");

        // Act & Assert
        // This should not throw an exception because the catch block in EmailServiceImpl catches it
        emailService.sendMessageUsingThymeleafTemplate("test@example.com", "Subject", "test-template", model);
    }

    /**
     * sendMessageUsingThymeleafTemplate luôn gắn tiền tố "email/" vào tên
     * template, nên mọi file .html nằm ngoài templates/email/ là file KHÔNG BAO
     * GIỜ gửi được - Thymeleaf ném TemplateInputException, và mọi nơi gọi email
     * đều bọc try/catch nên lỗi đó im lặng.
     *
     * Đúng chuyện đã xảy ra với booking-cancelled-auto.html: nó nằm thẳng trong
     * templates/, nên email báo hủy đơn tự động chưa bao giờ tới tay khách.
     */
    @Test
    void everyEmailTemplate_MustLiveUnderTemplatesEmail() throws Exception {
        java.io.File templatesDir = new java.io.File("src/main/resources/templates");
        java.io.File[] strayTemplates = templatesDir.listFiles(
                (dir, name) -> name.endsWith(".html"));

        org.assertj.core.api.Assertions.assertThat(strayTemplates)
                .as("template nằm ngoài templates/email/ thì không gửi được")
                .isEmpty();
    }
}
