package myproject.booking_tour.repository;

import myproject.booking_tour.entity.ContactMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContactMessageRepositoryTest {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Test
    void findAllByOrderByCreatedAtDesc_ShouldReturnMessagesInDescendingOrder() throws InterruptedException {
        // Arrange
        ContactMessage msg1 = new ContactMessage();
        msg1.setFullName("User One");
        msg1.setEmail("test1@test.com");
        msg1.setSubject("Subject 1");
        msg1.setMessage("Message 1");
        msg1.setCreatedAt(LocalDateTime.now().minusDays(2));
        contactMessageRepository.save(msg1);

        ContactMessage msg2 = new ContactMessage();
        msg2.setFullName("User Two");
        msg2.setEmail("test2@test.com");
        msg2.setSubject("Subject 2");
        msg2.setMessage("Message 2");
        msg2.setCreatedAt(LocalDateTime.now().minusDays(1));
        contactMessageRepository.save(msg2);

        // Act
        List<ContactMessage> result = contactMessageRepository.findAllByOrderByCreatedAtDesc();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("test2@test.com"); // Newer message first
        assertThat(result.get(1).getEmail()).isEqualTo("test1@test.com"); // Older message second
    }
}
