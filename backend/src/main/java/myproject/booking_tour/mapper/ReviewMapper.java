package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.request.ReviewRequest;
import myproject.booking_tour.dto.response.ReviewResponse;
import myproject.booking_tour.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toEntity(ReviewRequest request) {
        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return review;
    }

    public ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        if (review.getTour() != null) {
            response.setTourId(review.getTour().getId());
            response.setTourSlug(review.getTour().getSlug());
        }
        if (review.getUser() != null) {
            response.setUserId(review.getUser().getId());
            // KHONG tra username. GET /api/reviews/** la endpoint cong khai, va
            // username la mot nua thong tin dang nhap - voi tai khoan tao qua
            // Google, username CHINH LA dia chi email. Trang chu tung in username
            // lam ten tac gia danh gia, tuc la in email khach ra cho moi nguoi xem.
            response.setFullName(review.getUser().getFullName());
            response.setAvatar(review.getUser().getAvatar());
        }
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
