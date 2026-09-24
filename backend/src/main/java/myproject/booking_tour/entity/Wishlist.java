package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Mot tour ma nguoi dung danh dau yeu thich.
 *
 * Chi luu THAM CHIEU (user, tour) chu khong nhan ban title/price/imageUrl -
 * du lieu hien thi luon doc tuoi tu bang tours, nen tour doi gia thi wishlist
 * hien gia moi va tour bi xoa thi dong nay cung bi xoa theo (ON DELETE CASCADE).
 *
 * Khong dung @Data cua Lombok: entity co quan he ManyToOne LAZY, equals/hashCode
 * va toString tu sinh se keo theo viec nap toan bo doi tuong lien quan.
 */
@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Wishlist(User user, Tour tour) {
        this.user = user;
        this.tour = tour;
    }
}
