package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Cart;
import myproject.booking_tour.entity.CartItem;
import myproject.booking_tour.entity.Tour;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TourRepository tourRepository;

    @Test
    void save_ShouldPersistCartItem() {
        Cart cart = cartRepository.save(new Cart());

        Tour tour = new Tour();
        tour.setTitle("Test Tour");
        tour.setPrice(BigDecimal.valueOf(100));
        tourRepository.save(tour);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setTour(tour);
        item.setGuests(2);
        item.setStartDate(LocalDate.now().plusDays(5));

        CartItem saved = cartItemRepository.save(item);
        assertThat(saved.getId()).isNotNull();
    }
}
