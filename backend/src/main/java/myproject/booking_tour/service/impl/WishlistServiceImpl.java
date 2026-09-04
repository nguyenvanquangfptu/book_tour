package myproject.booking_tour.service.impl;

import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.entity.Wishlist;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.TourMapper;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.UserRepository;
import myproject.booking_tour.repository.WishlistRepository;
import myproject.booking_tour.service.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourMapper tourMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TourResponse> getWishlist(Long userId) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Wishlist::getTour)
                .map(tourMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TourResponse> addToWishlist(Long userId, Long tourId) {
        // Idempotent: bam tim hai lan khong tao ra hai dong. Rang buoc UNIQUE
        // (user_id, tour_id) o database van la lop chan cuoi cung neu hai
        // request den dong thoi va cung vuot qua kiem tra nay.
        if (!wishlistRepository.existsByUserIdAndTourId(userId, tourId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung: " + userId));
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tour: " + tourId));
            wishlistRepository.save(new Wishlist(user, tour));
        }
        return getWishlist(userId);
    }

    @Override
    @Transactional
    public List<TourResponse> removeFromWishlist(Long userId, Long tourId) {
        // Khong nem loi khi khong tim thay: bo mot thu von da khong co trong
        // danh sach thi ket qua cuoi cung van dung nhu mong doi.
        wishlistRepository.deleteByUserIdAndTourId(userId, tourId);
        return getWishlist(userId);
    }
}
