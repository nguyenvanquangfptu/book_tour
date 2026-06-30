package myproject.booking_tour.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
}
