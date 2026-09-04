package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;


import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "tours")
@SQLDelete(sql = "UPDATE tours SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(unique = true, length = 255)
    private String slug;

    @Column(length = 100)
    private String destination;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String duration;

    @Column(name = "image_url")
    private String imageUrl;

    @org.hibernate.annotations.Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> images = new java.util.ArrayList<>();



    @Column(name = "max_people")
    private Integer maxPeople;

    @Column(name = "available_slots")
    private Integer availableSlots;

    @Column(length = 50)
    private String status;

    @Column(name = "tour_type", length = 100)
    private String tourType;

    @Column(length = 100)
    private String transport;

    @org.hibernate.annotations.Formula("(SELECT COALESCE(SUM(b.number_of_people), 0) FROM bookings b WHERE b.tour_id = id)")
    private Integer bookedCount;

    /*
     * reviewCount va rating truoc day la HAI COT THAT trong bang tours, duoc
     * ReviewServiceImpl.updateTourRating() cap nhat bang tay moi khi co review
     * them/sua/xoa. Van de cua du lieu phi chuan hoa: khong co gi dam bao chung
     * dong bo - chi can mot luong nao do quen goi ham cap nhat la so lieu lech
     * vinh vien, va khong ai phat hien ra.
     *
     * Chuyen sang @Formula: Hibernate tinh truc tiep tu bang reviews o moi lan
     * SELECT nen KHONG BAO GIO lech. Cung cach ma bookedCount o tren dang lam.
     *
     * Danh doi: moi lan doc tour phai chay them subquery. Chap nhan duoc vi
     * reviews da co index idx_reviews_tour tren cot tour_id (migration V2).
     *
     * Hai truong nay CHI DOC - goi setter khong con y nghia gi.
     */
    @org.hibernate.annotations.Formula(
            "(SELECT COUNT(r.id) FROM reviews r WHERE r.tour_id = id)")
    private Integer reviewCount;

    @org.hibernate.annotations.Formula(
            "(SELECT COALESCE(AVG(CAST(r.rating AS DOUBLE PRECISION)), 0) FROM reviews r WHERE r.tour_id = id)")
    private Double rating;

    // Many-to-Many with Accommodations
    @ManyToMany
    @JoinTable(
            name = "tour_accommodations",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "accommodation_id")
    )
    private java.util.Set<Accommodation> accommodations = new java.util.HashSet<>();


    // Many-to-Many with Utilities
    @ManyToMany
    @JoinTable(
            name = "tour_utilities",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "utility_id")
    )
    private java.util.List<Utility> utilities = new java.util.ArrayList<>();

    @org.hibernate.annotations.Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(columnDefinition = "jsonb")
    private java.util.List<String> highlights = new java.util.ArrayList<>();

    @org.hibernate.annotations.Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(columnDefinition = "jsonb")
    private java.util.List<TourItinerary> itinerary = new java.util.ArrayList<>();

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;
}
