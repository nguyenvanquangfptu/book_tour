package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tour_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Version
    @Column(name = "version")
    private Integer version;
}
