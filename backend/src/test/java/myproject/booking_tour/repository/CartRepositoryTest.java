package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Cart;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("ROLE_USER");
        roleRepository.save(role);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword("pass");
        testUser.setRole(role);
        userRepository.save(testUser);

        Cart cart = new Cart();
        cart.setUser(testUser);
        cartRepository.save(cart);
    }

    @Test
    void findByUserId_ShouldReturnCart() {
        Optional<Cart> result = cartRepository.findByUserId(testUser.getId());
        assertThat(result).isPresent();
    }
}
