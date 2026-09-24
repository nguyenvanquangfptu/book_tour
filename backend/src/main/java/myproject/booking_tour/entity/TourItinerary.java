package myproject.booking_tour.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourItinerary implements Serializable {
    private String day;
    private String title;
    
    private String description;
}
