package myproject.booking_tour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourItinerary {
    private String day;
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
}
