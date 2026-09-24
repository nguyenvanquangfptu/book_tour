package myproject.booking_tour.repository.specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import myproject.booking_tour.entity.Tour;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TourSpecification {

    public static Specification<Tour> filterTours(String keyword, String destination, Integer durationDays, Integer guests, BigDecimal minPrice, BigDecimal maxPrice, String status, List<String> tourTypes, List<String> transports) {
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

            // Loc theo DIEM DEN, doi chieu voi chinh cot destination.
            //
            // Truoc day nhanh nay goi fts_match tren search_vector - y het nhanh
            // "keyword" ngay tren, giong nhau tung ky tu. Ma search_vector gop
            // title + destination + description, nen "loc theo diem den" thuc
            // chat la tim kiem toan van: destination=hang dong tra ve Ninh Binh
            // va Quang Binh (khop o mo ta), destination=bien tra ve 7 tour
            // (khop o tieu de), trong khi khong tour nao co diem den nhu vay.
            //
            // Dung LIKE chu khong phai bang tuyet doi vi o nhap diem den la
            // combobox go tu do co goi y, khong ep chon: nguoi dung go "Da" roi
            // gui luon van phai ra Da Nang va Da Lat.
            //
            // unaccent o CA HAI VE de go khong dau van tim duoc "Da Nang". Khu
            // dau ben Java khong dung duoc o day: Normalizer khong tach duoc
            // chu D gach ngang, con unaccent cua Postgres thi doi D -> D, hai
            // ben se khong bao gio khop nhau.
            if (destination != null && !destination.trim().isEmpty()) {
                Expression<String> normalizedColumn = criteriaBuilder.lower(
                        criteriaBuilder.function("unaccent", String.class, root.get("destination")));
                Expression<String> normalizedPattern = criteriaBuilder.lower(
                        criteriaBuilder.function("unaccent", String.class,
                                criteriaBuilder.literal("%" + destination.trim() + "%")));
                predicates.add(criteriaBuilder.like(normalizedColumn, normalizedPattern));
            }



            if (durationDays != null) {
                predicates.add(criteriaBuilder.like(root.get("duration"), durationDays + " %"));
            }

            if (guests != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("availableSlots"), guests));
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
