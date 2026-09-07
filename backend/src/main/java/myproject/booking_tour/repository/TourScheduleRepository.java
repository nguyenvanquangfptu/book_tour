package myproject.booking_tour.repository;

import myproject.booking_tour.entity.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    Optional<TourSchedule> findFirstByTourIdAndDepartureDate(Long tourId, LocalDate departureDate);

    java.util.List<TourSchedule> findByTourIdAndDepartureDateBetween(Long tourId, LocalDate startDate, LocalDate endDate);

    /**
     * Tao dong lich cho mot ngay khoi hanh neu chua co, khong lam gi neu da co.
     *
     * VI SAO KHONG DUNG "tim, neu null thi save": hai request cung dat mot ngay
     * chua co dong lich se cung doc ra rong, cung tao mot dong moi tru tu quota
     * DAY DU, roi cung INSERT. Rang buoc uq_tour_schedules_tour_date la thu duy
     * nhat chan duoc, va no chan bang cach nem loi 500 vao mat nguoi thu hai.
     *
     * ON CONFLICT DO NOTHING bien cuoc dua do thanh chuyen binh thuong: nguoi
     * thua cuoc chi don gian khong tao them dong nao, roi tru cho tren dong ma
     * nguoi kia vua tao.
     *
     * Khong khai bao cot xung dot de cau lenh chay duoc tren ca PostgreSQL
     * (production) lan H2 o che do PostgreSQL (test). INSERT nay chi co the
     * dung mot rang buoc duy nhat la (tour_id, departure_date) - id do database
     * tu sinh - nen bo qua muc tieu xung dot khong lam mat do chinh xac.
     *
     * @return 1 neu vua tao moi, 0 neu dong lich da ton tai
     */
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(value =
            "INSERT INTO tour_schedules (tour_id, departure_date, available_slots, version) " +
            "VALUES (:tourId, :departureDate, :defaultSlots, 0) " +
            "ON CONFLICT DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@org.springframework.data.repository.query.Param("tourId") Long tourId,
                       @org.springframework.data.repository.query.Param("departureDate") LocalDate departureDate,
                       @org.springframework.data.repository.query.Param("defaultSlots") int defaultSlots);

    /**
     * Tru cho cua mot ngay khoi hanh bang MOT cau lenh nguyen tu.
     *
     * Dieu kien "availableSlots >= :people" nam trong chinh cau UPDATE, nen viec
     * kiem tra va viec tru khong con la hai buoc tach roi de chen vao giua. Hai
     * request dong thoi khong bao gio ghi de len nhau: database khoa dong, ap
     * dung lan luot, va moi lan deu tinh tren so cho THUC TE tai thoi diem do.
     *
     * Truoc day doan nay la doc-sua-ghi tren entity, dua vao @Version de phat
     * hien xung dot. Nhung @Version chi PHAT HIEN chu khong giai quyet: khi
     * nam nguoi cung bam dat mot tour con thua cho cho ca nam, mot nguoi thanh
     * cong va bon nguoi con lai nhan 409 du khong he het cho.
     *
     * @return 1 neu da tru thanh cong, 0 neu khong du cho (hoac khong co dong
     *         lich nao cho ngay do)
     */
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(
            "UPDATE TourSchedule ts SET ts.availableSlots = ts.availableSlots - :people " +
            "WHERE ts.tour.id = :tourId AND ts.departureDate = :departureDate " +
            "AND ts.availableSlots >= :people")
    int deductSlots(@org.springframework.data.repository.query.Param("tourId") Long tourId,
                    @org.springframework.data.repository.query.Param("departureDate") LocalDate departureDate,
                    @org.springframework.data.repository.query.Param("people") int people);

    /**
     * Hoan cho cho MOI ngay ma tour dien ra, cung bang mot cau lenh nguyen tu.
     *
     * Duong hoan cho bat buoc phai cung co che voi duong tru cho. Neu de lai
     * doc-sua-ghi o day trong khi ben kia da chuyen sang UPDATE truc tiep, mot
     * lan huy chay song song voi mot lan dat se GHI DE ket qua cua lan dat -
     * cot version khong con nhich len nen khoa lac quan khong con phat hien
     * duoc gi nua.
     *
     * @return so dong lich da duoc hoan cho
     */
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(
            "UPDATE TourSchedule ts SET ts.availableSlots = ts.availableSlots + :people " +
            "WHERE ts.tour.id = :tourId AND ts.departureDate BETWEEN :startDate AND :endDate")
    int restoreSlots(@org.springframework.data.repository.query.Param("tourId") Long tourId,
                     @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
                     @org.springframework.data.repository.query.Param("endDate") LocalDate endDate,
                     @org.springframework.data.repository.query.Param("people") int people);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM TourSchedule ts WHERE ts.tour.id = :tourId")
    void deleteByTourId(@org.springframework.data.repository.query.Param("tourId") Long tourId);
}
