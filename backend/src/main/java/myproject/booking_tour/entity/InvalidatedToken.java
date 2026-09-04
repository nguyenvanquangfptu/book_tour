package myproject.booking_tour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "invalidated_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvalidatedToken {
    
    /**
     * SHA-256 cua token duoi dang hex - LUON dung 64 ky tu.
     * Truoc day cot nay luu chinh chuoi JWT: entity khai length = 1024 nhung
     * cot that trong database chi la VARCHAR(255), va Hibernate o che do
     * validate khong kiem tra do dai nen sai lech do khong bao gio lo ra luc
     * khoi dong - chi that bai am tham khi co user co username dai logout.
     */
    @Id
    @Column(length = 64)
    private String id;

    /** Dung cho TokenCleanupScheduler xoa cac ban ghi da het han. */
    private Date expiryTime;
}
