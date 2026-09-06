package myproject.booking_tour.repository;

import jakarta.persistence.LockModeType;
import myproject.booking_tour.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Tra token kem khoa ghi (SELECT ... FOR UPDATE).
     *
     * KHOA LA BAT BUOC, khong phai toi uu. Khi access token vua het han, trinh
     * duyet co the ban nhieu request cung luc va tat ca cung 401 -> nhieu lan
     * goi /refresh voi CUNG mot token. Khong co khoa thi cac giao dich cung doc
     * duoc trang thai "chua tieu" va cung ghi de len nhau; te hon nua la he
     * thong tu bao dong nham chinh minh dang bi tan cong.
     *
     * Khoa nay xep hang chung lai: request dau tien tieu token va commit,
     * request thu hai doc duoc usedAt != null. Frontend con mot lop chan nua
     * (single-flight trong axiosConfig.ts) nen truong hop nay hiem khi xay ra,
     * nhung server khong duoc phu thuoc vao viec client cu xu dung.
     *
     * Khong JOIN FETCH user o day: PostgreSQL khong cho FOR UPDATE tren ket qua
     * co join theo cach minh muon. User duoc nap LAZY ngay sau, van trong cung
     * giao dich.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RefreshToken t WHERE t.tokenHash = :hash")
    Optional<RefreshToken> lockByTokenHash(@Param("hash") String hash);

    /**
     * Thu hoi moi token con song trong mot family.
     *
     * PHAI CO CA HAI CO, va thu tu giua chung la ly do:
     *
     *   flushAutomatically - ghi cac thay doi dang cho xuong database TRUOC khi
     *       chay lenh UPDATE nay. Thieu no la mot loi da tung xay ra that:
     *       resetPassword() dat mat khau moi roi goi revokeAllForUser(); mat
     *       khau moi con nam trong persistence context chua duoc flush, lenh
     *       bulk chay thang xuong DB bo qua no, roi clearAutomatically xoa sach
     *       context - cuon theo ca mat khau moi lan lenh xoa ma OTP. API tra ve
     *       200 "doi mat khau thanh cong" trong khi mat khau khong he doi.
     *
     *   clearAutomatically - xoa context SAU khi chay, vi lenh UPDATE khong di
     *       qua persistence context nen cac thuc the dang quan ly se giu trang
     *       thai cu neu khong xoa.
     *
     * Loai loi nay VO HINH voi test dung mock: khong co persistence context that
     * thi khong co gi de flush hay clear. Chi test tich hop moi bat duoc - xem
     * RefreshTokenRepositoryTest.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE RefreshToken t
               SET t.revokedAt = :now, t.revokedReason = :reason
             WHERE t.familyId = :familyId
               AND t.revokedAt IS NULL
            """)
    int revokeFamily(@Param("familyId") UUID familyId,
                     @Param("now") LocalDateTime now,
                     @Param("reason") RefreshToken.RevocationReason reason);

    /**
     * Family nay da tung bi gan co REUSE_DETECTED chua?
     *
     * Dung de mot vu xam nhap chi sinh DUNG MOT email canh bao. Ke tan cong (va
     * ca nguoi dung that) thuong thu lai nhieu lan sau khi bi cat quyen; moi lan
     * do deu la mot phat hien dung lai hop le, nhung tat ca cung thuoc mot su
     * kien. Gui email cho tung lan thi hop thu day thong bao trung nhau va
     * nguoi dung se hoc cach bo qua chung.
     */
    boolean existsByFamilyIdAndRevokedReason(UUID familyId, RefreshToken.RevocationReason revokedReason);

    /** Thu hoi moi phien cua mot nguoi - dung khi doi mat khau. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE RefreshToken t
               SET t.revokedAt = :now, t.revokedReason = :reason
             WHERE t.user.id = :userId
               AND t.revokedAt IS NULL
            """)
    int revokeAllForUser(@Param("userId") Long userId,
                         @Param("now") LocalDateTime now,
                         @Param("reason") RefreshToken.RevocationReason reason);

    /**
     * Xoa han cac ban ghi da het han. An toan voi khoa ngoai parent_id vi cot do
     * la ON DELETE SET NULL, va vi ca family chung mot expires_at nen thuong bi
     * xoa tron mot luot.
     *
     * @Transactional dat ngay day de moi lenh don la mot giao dich doc lap -
     * TokenCleanupScheduler co y khong mo transaction bao ca hai bang.
     */
    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt <= :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
