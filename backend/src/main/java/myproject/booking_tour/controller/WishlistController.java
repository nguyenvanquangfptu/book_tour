package myproject.booking_tour.controller;

import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.security.CustomUserDetails;
import myproject.booking_tour.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getId();
        }
        throw new myproject.booking_tour.exception.BadRequestException("User not authenticated");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TourResponse>>> getWishlist() {
        List<TourResponse> wishlist = wishlistService.getWishlist(getAuthenticatedUserId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách yêu thích thành công", wishlist));
    }

    @PostMapping("/{tourId}")
    public ResponseEntity<ApiResponse<List<TourResponse>>> addToWishlist(@PathVariable Long tourId) {
        List<TourResponse> wishlist = wishlistService.addToWishlist(getAuthenticatedUserId(), tourId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã thêm vào danh sách yêu thích", wishlist));
    }

    @DeleteMapping("/{tourId}")
    public ResponseEntity<ApiResponse<List<TourResponse>>> removeFromWishlist(@PathVariable Long tourId) {
        List<TourResponse> wishlist = wishlistService.removeFromWishlist(getAuthenticatedUserId(), tourId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã bỏ khỏi danh sách yêu thích", wishlist));
    }
}
