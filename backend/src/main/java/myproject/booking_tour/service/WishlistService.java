package myproject.booking_tour.service;

import myproject.booking_tour.dto.response.TourResponse;

import java.util.List;

public interface WishlistService {

    /** Danh sach tour yeu thich cua nguoi dung, moi nhat truoc. */
    List<TourResponse> getWishlist(Long userId);

    /** Them tour vao wishlist. Da co roi thi khong lam gi (idempotent). */
    List<TourResponse> addToWishlist(Long userId, Long tourId);

    /** Bo tour khoi wishlist. Khong co san cung khong bao loi (idempotent). */
    List<TourResponse> removeFromWishlist(Long userId, Long tourId);
}
