package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String destination;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String duration;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "tour_images", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "image_url", length = 1000)
    private List<String> images = new java.util.ArrayList<>();

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

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

    @org.hibernate.annotations.Formula("(SELECT COUNT(r.id) FROM reviews r WHERE r.tour_id = id)")
    private Integer reviewCount;

    @org.hibernate.annotations.Formula("(SELECT COALESCE(AVG(r.rating), 0) FROM reviews r WHERE r.tour_id = id)")
    private Double rating;

    // Many-to-One with Accommodation (Ref: tours.accommodation_id > accommodations.id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    // Many-to-Many with Utilities
    @ManyToMany
    @JoinTable(
            name = "tour_utilities",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "utility_id")
    )
    private Set<Utility> utilities = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "tour_highlights", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "highlight")
    private java.util.List<String> highlights = new java.util.ArrayList<>();


    @ElementCollection
    @CollectionTable(name = "tour_itineraries", joinColumns = @JoinColumn(name = "tour_id"))
    private java.util.List<TourItinerary> itinerary = new java.util.ArrayList<>();
}