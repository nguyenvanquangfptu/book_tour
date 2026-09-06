package myproject.booking_tour.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booking_tour.entity.RefreshToken;
import myproject.booking_tour.entity.RefreshToken.RevocationReason;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.TokenReuseException;
import myproject.booking_tour.exception.UnauthorizedException;
import myproject.booking_tour.repository.RefreshTokenRepository;
import myproject.booking_tour.security.ClientMetadata;
import myproject.booking_tour.security.TokenHasher;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Vong doi cua refresh token: cap, xoay vong mot lan dung, thu hoi.
 *
 * Ba quy tac chi phoi toan bo lop nay:
 *
 *   1. MOT LAN DUNG. Token nao da doi lay access token thi chet ngay tai do,
 *      thay bang mot token moi.
 *   2. FAMILY. Ca chuoi token sinh ra tu mot lan dang nhap mang chung familyId
 *      - day la don vi bi thu hoi, khong phai tung token le.
 *   3. DUNG LAI = BI SAO CHEP. Token da tieu ma quay lai thi ban goc cua no da
 *      lot ra ngoai. Server khong the biet lan nay la chu that hay ke trom, nen
 *      thu hoi ca family va bao cho chu tai khoan.
 *
 * Token la chuoi ngau nhien chu KHONG phai JWT. JWT tu chung minh duoc tinh hop
 * le ma khong can hoi database, nghia la khong co cho nao de cuong che quy tac 1.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    /** 32 byte = 256 bit ngau nhien. Base64 URL khong dem -> chuoi 43 ky tu. */
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    /** Ban goc cua token - gia tri duy nhat trong he thong khong duoc luu lai. */
    public record IssuedToken(String rawValue, LocalDateTime expiresAt) {
    }

    /** Ket qua xoay vong: chu tai khoan (de dung access token moi) va token ke tiep. */
    public record Rotation(User user, IssuedToken token) {
    }

    /**
     * Mo mot phien moi (dang nhap / dang ky / dang nhap Google): familyId moi,
     * khong co token cha, han 30 ngay tinh tu bay gio.
     */
    @Transactional
    public IssuedToken startSession(User user, ClientMetadata client) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusNanos(refreshExpirationMs * 1_000_000L);
        return persist(user, UUID.randomUUID(), null, now, expiresAt, client);
    }

    /**
     * Doi mot refresh token lay token ke tiep.
     *
     * noRollbackFor: khi phat hien dung lai, phuong thuc nay THU HOI ca family
     * roi moi nem ngoai le. Neu de ngoai le lam giao dich quay lui thi viec thu
     * hoi bi xoa sach va ke tan cong van refresh tiep duoc - dung co che phat
     * hien lai thanh vo dung. Xem them TokenReuseException.
     */
    @Transactional(noRollbackFor = TokenReuseException.class)
    public Rotation rotate(String rawToken, ClientMetadata client) {
        // Khoa ghi: chan hai request cung doi mot token va cung thay no chua tieu.
        RefreshToken current = refreshTokenRepository
                .lockByTokenHash(TokenHasher.sha256Hex(rawToken))
                .orElseThrow(() -> new UnauthorizedException(
                        "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại."));

        LocalDateTime now = LocalDateTime.now();

        // THU TU BA PHEP KIEM TRA DUOI DAY LA CO Y. Gop chung lai hoac dao cho
        // deu sinh ra bao dong gia - xem giai thich o tung buoc.

        // 1. Het han truoc tien. Sau 30 ngay ca family deu chet, viec token quay
        //    lai khong noi len dieu gi - mot dien thoai offline lau ngay bat len
        //    la du. Bao dong o day chi lam nguoi dung hoang so vo co.
        if (current.isExpiredAt(now)) {
            throw new UnauthorizedException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }

        // 2. DA TUNG DUOC DUNG. Day la bang chung duy nhat cho thay token bi sao
        //    chep: no da doi lay token moi roi ma van co nguoi cam ban goc gui
        //    len. Phai kiem TRUOC isRevoked(), vi xoay vong binh thuong danh dau
        //    ca hai co - dao lai thi token bi trom se roi vao nhanh 401 im lang
        //    va co che phat hien tat ngom.
        if (current.isUsed()) {
            handleReuse(current, client, now);
            throw new TokenReuseException(
                    "Phát hiện truy cập bất thường. Toàn bộ phiên đăng nhập đã bị thu hồi, "
                            + "vui lòng đăng nhập lại.");
        }

        // 3. Bi thu hoi nhung CHUA TUNG dung. Nguyen nhan luon la do he thong:
        //    logout, doi mat khau, hoac day chuyen thu hoi cua mot vu da duoc
        //    canh bao truoc do. Tu choi im lang, khong gui email - khong co gi
        //    moi de bao.
        if (current.isRevoked()) {
            throw new UnauthorizedException("Phiên đăng nhập không còn hiệu lực. Vui lòng đăng nhập lại.");
        }

        User user = current.getUser();
        // Nap that su truoc khi giao dich dong: ben goi con doc email/role de dung
        // AuthResponse, luc do proxy LAZY khong con nap duoc nua.
        Hibernate.initialize(user);

        current.setUsedAt(now);
        current.setRevokedAt(now);
        current.setRevokedReason(RevocationReason.ROTATED);

        // Han cua token moi la han CUA FAMILY, khong phai now + 30 ngay: xoay
        // vong khong duoc phep keo dai phien vo han.
        IssuedToken next = persist(user, current.getFamilyId(), current.getId(),
                now, current.getExpiresAt(), client);

        return new Rotation(user, next);
    }

    /**
     * Dang xuat: thu hoi ca family chu khong chi token dang cam.
     *
     * Token khong tim thay thi im lang bo qua - co the no da duoc xoay vong o
     * tab khac, va dang xuat khong phai cho nem loi vao mat nguoi dung.
     */
    @Transactional
    public void revokeSession(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.lockByTokenHash(TokenHasher.sha256Hex(rawToken))
                .ifPresent(token -> refreshTokenRepository.revokeFamily(
                        token.getFamilyId(), LocalDateTime.now(), RevocationReason.LOGOUT));
    }

    /** Doi mat khau thi moi phien tren moi thiet bi deu phai dang nhap lai. */
    @Transactional
    public void revokeAllSessions(Long userId, RevocationReason reason) {
        int revoked = refreshTokenRepository.revokeAllForUser(userId, LocalDateTime.now(), reason);
        if (revoked > 0) {
            log.info("Da thu hoi {} refresh token cua user {} - ly do {}", revoked, userId, reason);
        }
    }

    private IssuedToken persist(User user, UUID familyId, Long parentId,
                                LocalDateTime issuedAt, LocalDateTime expiresAt,
                                ClientMetadata client) {
        byte[] raw = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(raw);
        String rawValue = encoder.encodeToString(raw);

        RefreshToken token = new RefreshToken();
        token.setTokenHash(TokenHasher.sha256Hex(rawValue));
        token.setUser(user);
        token.setFamilyId(familyId);
        token.setParentId(parentId);
        token.setIssuedAt(issuedAt);
        token.setExpiresAt(expiresAt);
        token.setIpAddress(client.ipAddress());
        token.setUserAgent(client.userAgent());
        refreshTokenRepository.save(token);

        return new IssuedToken(rawValue, expiresAt);
    }

    /**
     * Mot token da tieu vua quay lai. Doc du lieu nguoi dung TRUOC khi goi
     * revokeFamily - lenh do la bulk update co clearAutomatically, sau no moi
     * thuc the dang quan ly deu bi go khoi persistence context.
     */
    private void handleReuse(RefreshToken token, ClientMetadata client, LocalDateTime now) {
        User user = token.getUser();
        Long userId = user.getId();
        String email = user.getEmail();
        String fullName = user.getFullName();
        UUID familyId = token.getFamilyId();
        LocalDateTime issuedAt = token.getIssuedAt();
        LocalDateTime spentAt = token.getUsedAt();
        Long tokenId = token.getId();

        // Hoi TRUOC khi thu hoi: sau lenh duoi thi ca family deu mang co
        // REUSE_DETECTED va khong con phan biet duoc day co phai lan dau khong.
        boolean alreadyAlerted =
                refreshTokenRepository.existsByFamilyIdAndRevokedReason(familyId, RevocationReason.REUSE_DETECTED);

        int revoked = refreshTokenRepository.revokeFamily(familyId, now, RevocationReason.REUSE_DETECTED);

        // Van ghi log MOI lan: tung lan thu lai deu la du lieu dieu tra (ip nao,
        // luc may gio). Chi rieng email moi phai han che.
        log.error("PHAT HIEN DUNG LAI REFRESH TOKEN | user={} family={} tokenId={} capLuc={} "
                        + "daTieuLuc={} | benGoi ip={} userAgent={} | da thu hoi {} token | daCanhBaoTruocDo={}",
                userId, familyId, tokenId, issuedAt, spentAt,
                client.ipAddress(), client.userAgent(), revoked, alreadyAlerted);

        if (!alreadyAlerted) {
            sendAlert(email, fullName, client, now);
        }
    }

    private void sendAlert(String email, String fullName, ClientMetadata client, LocalDateTime detectedAt) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("fullName", fullName);
            model.put("ipAddress", client.ipAddress() == null ? "không xác định" : client.ipAddress());
            model.put("userAgent", client.userAgent() == null ? "không xác định" : client.userAgent());
            model.put("detectedAt", detectedAt);
            emailService.sendMessageUsingThymeleafTemplate(
                    email,
                    "Cảnh báo bảo mật: phiên đăng nhập của bạn đã bị thu hồi",
                    "security-alert",
                    model);
        } catch (Exception e) {
            // Gui mail hong khong duoc lam hong viec thu hoi - do moi la phan bao
            // ve that su. Canh bao chi la thong bao them.
            log.error("Khong gui duoc email canh bao bao mat toi {}: {}", email, e.getMessage());
        }
    }
}
