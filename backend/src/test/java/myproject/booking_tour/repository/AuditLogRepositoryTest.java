package myproject.booking_tour.repository;

import myproject.booking_tour.entity.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void save_ShouldPersistAuditLog() {
        // Arrange
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("CREATE");
        auditLog.setEntityName("Tour");
        auditLog.setEntityId(1L);

        // Act
        AuditLog saved = auditLogRepository.save(auditLog);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAction()).isEqualTo("CREATE");
    }
}
