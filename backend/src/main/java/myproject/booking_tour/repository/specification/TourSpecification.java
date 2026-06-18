package myproject.booking_tour.repository.specification;

import jakarta.persistence.criteria.Predicate;
import myproject.booking_tour.entity.Tour;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TourSpecification {

    private static final String VN_ACCENTS = "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ";
    private static final String VN_NO_ACCENTS = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd";

    public static String removeAccents(String str) {
        if (str == null) return null;
        String temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        return temp.replaceAll("\\p{M}", "").replace("đ", "d").replace("Đ", "D");
    }

    public static Specification<Tour> filterTours(String keyword, String destination, BigDecimal minPrice, BigDecimal maxPrice, String status, List<String> tourTypes, List<String> transports) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + removeAccents(keyword.trim().toLowerCase()) + "%";
                jakarta.persistence.criteria.Expression<String> titleLower = criteriaBuilder.lower(root.get("title"));
                jakarta.persistence.criteria.Expression<String> titleUnaccent = criteriaBuilder.function("translate", String.class, titleLower, criteriaBuilder.literal(VN_ACCENTS), criteriaBuilder.literal(VN_NO_ACCENTS));
                
                jakarta.persistence.criteria.Expression<String> descLower = criteriaBuilder.lower(root.get("description"));
                jakarta.persistence.criteria.Expression<String> descUnaccent = criteriaBuilder.function("translate", String.class, descLower, criteriaBuilder.literal(VN_ACCENTS), criteriaBuilder.literal(VN_NO_ACCENTS));
                
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(titleUnaccent, likeKeyword),
                        criteriaBuilder.like(descUnaccent, likeKeyword)
                ));
            }

            if (destination != null && !destination.trim().isEmpty()) {
                String likeDest = "%" + removeAccents(destination.trim().toLowerCase()) + "%";
                jakarta.persistence.criteria.Expression<String> destLower = criteriaBuilder.lower(root.get("destination"));
                jakarta.persistence.criteria.Expression<String> destUnaccent = criteriaBuilder.function("translate", String.class, destLower, criteriaBuilder.literal(VN_ACCENTS), criteriaBuilder.literal(VN_NO_ACCENTS));
                predicates.add(criteriaBuilder.like(destUnaccent, likeDest));
            }



            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (status != null && !status.trim().isEmpty()) {
                if (status.contains(",")) {
                    List<String> statusList = java.util.Arrays.asList(status.split(","));
                    predicates.add(root.get("status").in(statusList));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
            } else {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.notEqual(root.get("status"), "DELETED"),
                        criteriaBuilder.isNull(root.get("status"))
                ));
            }

            if (tourTypes != null && !tourTypes.isEmpty()) {
                predicates.add(root.get("tourType").in(tourTypes));
            }

            if (transports != null && !transports.isEmpty()) {
                predicates.add(root.get("transport").in(transports));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
