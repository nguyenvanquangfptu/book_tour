package myproject.booking_tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.request.CartItemRequest;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.CartResponse;
import myproject.booking_tour.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private Long getAuthenticatedUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof myproject.booking_tour.security.CustomUserDetails) {
            return ((myproject.booking_tour.security.CustomUserDetails) authentication.getPrincipal()).getUser().getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        Long userId = getAuthenticatedUserId();
        CartResponse cart = cartService.getCartForUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy giỏ hàng thành công", cart));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@Valid @RequestBody CartItemRequest request) {
        Long userId = getAuthenticatedUserId();
        CartResponse cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Thêm vào giỏ hàng thành công", cart));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(@PathVariable Long itemId) {
        Long userId = getAuthenticatedUserId();
        CartResponse cart = cartService.removeFromCart(userId, itemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã xóa khỏi giỏ hàng", cart));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        Long userId = getAuthenticatedUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã làm sạch giỏ hàng", null));
    }
}
