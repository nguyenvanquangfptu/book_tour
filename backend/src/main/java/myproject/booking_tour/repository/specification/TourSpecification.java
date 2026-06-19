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
                predicates.add(criteriaBuilder.isTrue(
                    criteriaBuilder.function("fts_match", Boolean.class, 
                        root.get("searchVector"), 
                        criteriaBuilder.literal(keyword.trim())
                    )
                ));
            }

            if (destination != null && !destination.trim().isEmpty()) {
                predicates.add(criteriaBuilder.isTrue(
                    criteriaBuilder.function("fts_match", Boolean.class, 
                        root.get("searchVector"), 
                        criteriaBuilder.literal(destination.trim())
                    )
                ));
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
