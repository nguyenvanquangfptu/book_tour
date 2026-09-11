package myproject.booking_tour.service;

import myproject.booking_tour.dto.request.CartItemRequest;
import myproject.booking_tour.dto.response.CartItemResponse;
import myproject.booking_tour.dto.response.CartResponse;
import myproject.booking_tour.entity.Cart;
import myproject.booking_tour.entity.CartItem;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.repository.CartItemRepository;
import myproject.booking_tour.repository.CartRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private myproject.booking_tour.repository.TourScheduleRepository tourScheduleRepository;

    @Transactional
    public CartResponse getCartForUser(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(Long userId, CartItemRequest request) {
        // Ba ngoai le duoi day deu la loi cua BEN GOI, khong phai su co he
        // thong. IllegalArgumentException va RuntimeException tran roi vao
        // handler cuoi cua GlobalExceptionHandler: khach nhan 500 kem "Da co
        // loi xay ra, vui long thu lai sau!" nen khong biet minh sai o dau, con
        // log thi day stack trace muc ERROR cua nhung tinh huong hoan toan binh
        // thuong. Thong bao cung chuyen sang tieng Viet cho khop voi phan con
        // lai cua luong dat tour.
        if (request.getGuests() == null || request.getGuests() <= 0) {
            throw new myproject.booking_tour.exception.BadRequestException("Số lượng khách phải lớn hơn 0.");
        }

        Cart cart = getOrCreateCart(userId);
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new myproject.booking_tour.exception.ResourceNotFoundException(
                        "Tour not found with id: " + request.getTourId()));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getTour().getId().equals(tour.getId()) && item.getStartDate().equals(request.getStartDate()))
                .findFirst();

        int totalGuests = request.getGuests();
        if (existingItem.isPresent()) {
            totalGuests += existingItem.get().getGuests();
        }

        int availableSlots = tour.getAvailableSlots() != null ? tour.getAvailableSlots() : 0;
        Optional<myproject.booking_tour.entity.TourSchedule> schedule = tourScheduleRepository.findFirstByTourIdAndDepartureDate(tour.getId(), request.getStartDate());
        if (schedule.isPresent()) {
            availableSlots = schedule.get().getAvailableSlots() != null ? schedule.get().getAvailableSlots() : 0;
        }

        if (totalGuests > availableSlots) {
            throw new myproject.booking_tour.exception.BadRequestException(
                    "Ngày khởi hành này không đủ chỗ cho " + totalGuests + " khách, chỉ còn "
                            + availableSlots + " chỗ trống.");
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setGuests(totalGuests);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setTour(tour);
            newItem.setGuests(request.getGuests());
            newItem.setStartDate(request.getStartDate());
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new myproject.booking_tour.exception.ResourceNotFoundException(
                            "User not found with id: " + userId));
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setItems(cart.getItems().stream().map(this::mapToCartItemResponse).collect(Collectors.toList()));
        return response;
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setTourId(item.getTour().getId());
        response.setTourTitle(item.getTour().getTitle());
        response.setPrice(item.getTour().getPrice());
        response.setImageUrl(item.getTour().getImageUrl());
        response.setGuests(item.getGuests());
        response.setStartDate(item.getStartDate());
        return response;
    }
}
