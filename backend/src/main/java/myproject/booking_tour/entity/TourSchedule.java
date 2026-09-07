package myproject.booking_tour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
    name = "tour_schedules",
    // Rang buoc nay da co trong production tu migration V3, nhung schema cua
    // test do Hibernate tu sinh tu entity (xem src/test/resources) - khong khai
    // bao o day thi test chay tren mot schema LONG HON production, va cuoc dua
    // "hai request cung tao dong lich cho mot ngay" se khong bao gio tai hien
    // duoc trong test. Ten dat trung ten trong V3.
    uniqueConstraints = @UniqueConstraint(
        name = "uq_tour_schedules_tour_date",
        columnNames = {"tour_id", "departure_date"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    /**
     * LUU Y: khoa lac quan KHONG con la thu bao ve so cho nua.
     *
     * Viec tru va hoan cho deu di qua cac cau UPDATE nguyen tu trong
     * TourScheduleRepository (deductSlots / restoreSlots), la nhung cau lenh
     * khong the mat ghi va cung khong bao gio sinh ra xung dot phai thu lai.
     *
     * Cot version giu lai de bao ve cac duong ghi theo entity con lai (vi du
     * admin sua so cho cua mot ngay). Bat cu doan code moi nao muon thay doi
     * availableSlots deu nen dung hai phuong thuc tren thay vi doc - sua - ghi.
     */
    @Version
    @Column(name = "version")
    private Integer version;
}
