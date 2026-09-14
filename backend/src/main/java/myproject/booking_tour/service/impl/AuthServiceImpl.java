package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.LoginRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.response.AuthResponse;
import myproject.booking_tour.dto.response.AuthResult;
import myproject.booking_tour.entity.RefreshToken;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.exception.UnauthorizedException;
import myproject.booking_tour.repository.RoleRepository;
import myproject.booking_tour.repository.UserRepository;
import myproject.booking_tour.service.AuthService;
import myproject.booking_tour.service.RefreshTokenService;
import myproject.booking_tour.security.ClientMetadata;
import myproject.booking_tour.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final myproject.booking_tour.repository.PasswordResetTokenRepository tokenRepository;
    private final myproject.booking_tour.service.EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Ba cua ngo vao he thong - dang ky, dang nhap, dang nhap Google - deu ket
     * thuc o day: mot access token 15 phut kem mot phien refresh 30 ngay moi.
     */
    private AuthResult startSession(User user, ClientMetadata client) {
        RefreshTokenService.IssuedToken refresh = refreshTokenService.startSession(user, client);
        return new AuthResult(toAuthResponse(user), refresh.rawValue(), refresh.expiresAt());
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user.getUsername()),
                user.getId(),
                user.getUsername(),
                user.getRole() != null ? user.getRole().getName() : "CUSTOMER",
                user.getFullName(),
                user.getEmail(),
                user.getAvatar()
        );
    }

    @Override
    @Transactional
    public AuthResult register(RegisterRequest request, ClientMetadata client) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        // Fetch or create default CUSTOMER role
        Role role = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("CUSTOMER");
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encode password
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(role);

        User savedUser = userRepository.save(user);

        return startSession(savedUser, client);
    }

    @Override
    @Transactional
    public AuthResult login(LoginRequest request, ClientMetadata client) {
        User user = userRepository.findByUsernameOrEmailIgnoreCase(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password!"));

        // Check password matching
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password!");
        }

        return startSession(user, client);
    }

    /**
     * Doi refresh token lay cap token moi.
     *
     * KHONG dat @Transactional o day. RefreshTokenService.rotate() da tu quan ly
     * giao dich cua no voi noRollbackFor(TokenReuseException) - boc them mot
     * giao dich ben ngoai se khien ngoai le do lam quay lui viec thu hoi family,
     * dung co che phat hien dung lai thanh vo dung.
     */
    @Override
    public AuthResult refresh(String refreshToken, ClientMetadata client) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Không tìm thấy phiên đăng nhập. Vui lòng đăng nhập lại.");
        }
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(refreshToken, client);
        return new AuthResult(
                toAuthResponse(rotation.user()),
                rotation.token().rawValue(),
                rotation.token().expiresAt()
        );
    }

    @org.springframework.beans.factory.annotation.Value("${google.client.id:YOUR_GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResult loginWithGoogle(String idTokenString, ClientMetadata client) {
        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = verifyGoogleToken(idTokenString);

        // Email trong token chi chung minh duoc danh tinh khi Google da xac minh
        // no. Mot tai khoan Google co the dang ky bang MOT DIA CHI BAT KY ma
        // khong can chung minh la chu hop thu - luc do token van hop le, van mang
        // dung dia chi do, chi co email_verified = false.
        //
        // Truoc day dong nay khong ton tai, va ngay ben duoi la tim tai khoan
        // theo email roi cap phien luon. Ai biet email cua nguoi khac - ke ca
        // email cua ADMIN - chi can tao mot tai khoan Google voi dia chi do la
        // dang nhap thang vao tai khoan cua ho, khong can mat khau.
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            log.warn("Tu choi dang nhap Google: email trong token chua duoc Google xac minh");
            throw new UnauthorizedException("Email của tài khoản Google này chưa được xác minh.");
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            Role role = roleRepository.findByName("CUSTOMER")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName("CUSTOMER");
                        return roleRepository.save(newRole);
                    });

            if (name == null || name.trim().isEmpty()) {
                name = email.substring(0, email.indexOf("@"));
            }

            String username = email;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = email.substring(0, email.indexOf("@")) + counter;
                counter++;
            }

            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(name);
            user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString())); // Random password
            user.setRole(role);
            user = userRepository.save(user);
        }

        return startSession(user, client);
    }

    /**
     * Xac minh chu ky, audience va han cua ID token Google.
     *
     * Chi phan nay moi duoc tra 401. Truoc day mot khoi try bat Exception boc
     * ca than ham dang nhap, nen moi su co phia sau - database mat ket noi luc
     * tao tai khoan, loi luc cap refresh token - deu thanh 401 "Failed to verify
     * Google Token: ..." kem nguyen van thong bao cua tang duoi, va duoc in ra
     * bang printStackTrace thay vi di qua log.
     *
     * Khong private de test thay duoc cuoc goi mang toi Google.
     */
    com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken;
        try {
            com.google.api.client.http.HttpTransport transport = new com.google.api.client.http.javanet.NetHttpTransport();
            com.google.api.client.json.JsonFactory jsonFactory = new com.google.api.client.json.gson.GsonFactory();
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier = new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            log.warn("Khong xac minh duoc ID token Google: {}", e.getMessage());
            throw new UnauthorizedException("Không xác minh được tài khoản Google.");
        }
        if (idToken == null) {
            throw new UnauthorizedException("Invalid ID token.");
        }
        return idToken.getPayload();
    }



    @Override
    @Transactional
    public void forgotPassword(String email) {
        // KHONG BAO GIO tiet lo email co ton tai hay khong.
        //
        // Truoc day dong nay la .orElseThrow(ResourceNotFoundException) va tra
        // ve 404 kem nguyen van "User not found with email: x@y.com" - bien
        // endpoint nay thanh cong cu do danh sach tai khoan da dang ky. Cau
        // "sent to email if it exists" ma controller tra ve khi do la vo nghia.
        //
        // Moi nhanh duoi day deu ket thuc bang return binh thuong. Ly do that
        // su chi di vao log cua may chu.
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            log.info("Yeu cau khoi phuc mat khau cho email khong ton tai - bo qua im lang");
            return;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Dang bi khoa do yeu cau qua nhieu lan
        if (user.getForgotPasswordBanUntil() != null && user.getForgotPasswordBanUntil().isAfter(now)) {
            log.warn("User {} dang bi khoa khoi phuc mat khau den {}", user.getId(), user.getForgotPasswordBanUntil());
            return;
        }

        // Khoa da het han -> cho lam lai tu dau
        if (user.getForgotPasswordBanUntil() != null && user.getForgotPasswordBanUntil().isBefore(now)) {
            user.setForgotPasswordAttempts(0);
            user.setForgotPasswordBanUntil(null);
        }

        // Cooldown 1 phut giua hai lan xin ma
        if (user.getForgotPasswordLastAttempt() != null
                && java.time.Duration.between(user.getForgotPasswordLastAttempt(), now).toMinutes() < 1) {
            log.info("User {} xin ma lai qua som - bo qua", user.getId());
            return;
        }

        int attempts = user.getForgotPasswordAttempts() == null ? 0 : user.getForgotPasswordAttempts();
        user.setForgotPasswordAttempts(attempts + 1);
        user.setForgotPasswordLastAttempt(now);

        if (user.getForgotPasswordAttempts() >= 3) {
            user.setForgotPasswordBanUntil(now.plusHours(5));
        }

        userRepository.save(user);

        // Moi user chi co dung mot ma song tai mot thoi diem
        tokenRepository.deleteByUser(user);

        // SecureRandom chu khong phai Random: day la gia tri doi duoc mat khau.
        String otp = String.format("%06d", new java.security.SecureRandom().nextInt(1000000));

        // Database chi luu SHA-256 cua ma, khong luu ban goc - cung ly do nhu
        // refresh_tokens. Ban goc chi ton tai trong email gui di.
        myproject.booking_tour.entity.PasswordResetToken resetToken =
                new myproject.booking_tour.entity.PasswordResetToken(
                        myproject.booking_tour.security.TokenHasher.sha256Hex(otp),
                        user,
                        now.plusMinutes(5));
        tokenRepository.save(resetToken);

        // KHONG BAO GIO ghi ma OTP vao log. Truoc day o day co mot dong log.info
        // in thang ma kem email - ai doc duoc logs/app.log la chiem duoc moi tai
        // khoan. Log bi doi xu nhu du lieu khong nhay cam: no duoc day len dich
        // vu tap trung, duoc sao luu, duoc dan ra khi debug.
        log.info("Da tao ma khoi phuc mat khau cho user {} (het han {})", user.getId(), now.plusMinutes(5));

        try {
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("resetToken", otp);
            emailService.sendMessageUsingThymeleafTemplate(user.getEmail(), "Khôi phục mật khẩu", "reset-password", model);
        } catch (Exception e) {
            log.error("Failed to send reset password email: {}", e.getMessage());
        }
    }

    /** Mot ma OTP chiu toi da bay nhieu lan nhap sai truoc khi bi huy. */
    static final int MAX_RESET_CODE_ATTEMPTS = 5;

    /**
     * Mot cau cho moi truong hop that bai: email khong ton tai, chua xin ma, ma
     * sai, ma het han, ma vua bi huy vi sai qua nhieu. Noi khac nhau la bao cho
     * nguoi ngoai biet email nao co tai khoan.
     */
    private static final String INVALID_RESET_CODE =
            "Mã xác nhận không đúng hoặc đã hết hạn. Vui lòng kiểm tra lại hoặc yêu cầu mã mới.";

    /**
     * Doi mat khau bang ma OTP gui qua email.
     *
     * Ma chi duoc doi chieu voi ma cua DUNG tai khoan mang email do, va moi ma
     * chiu toi da MAX_RESET_CODE_ATTEMPTS lan sai. Truoc day ham nay chi nhan ma,
     * tra no tren toan bang va khong dem gi ca: ma 6 so co mot trieu gia tri,
     * moi lan doan thu cung luc voi ma cua moi nguoi dang xin khoi phuc, va tran
     * duy nhat la gioi han 10 request/phut moi IP.
     *
     * noRollbackFor: so lan sai phai duoc GHI LAI ngay ca khi ham nem loi. Neu
     * de BadRequestException quay lui giao dich thi bo dem khong bao gio tang.
     */
    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public void resetPassword(String email, String token, String newPassword) {
        User owner = email == null ? null : userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        myproject.booking_tour.entity.PasswordResetToken resetToken = owner == null
                ? null
                : tokenRepository.findFirstByUserOrderByIdDesc(owner).orElse(null);
        if (resetToken == null) {
            throw new BadRequestException(INVALID_RESET_CODE);
        }

        if (resetToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException(INVALID_RESET_CODE);
        }

        // Database chi luu SHA-256 cua ma. So sanh thoi gian hang so de do thoi
        // gian phan hoi khong noi duoc ma dung bao nhieu ky tu dau.
        boolean matches = java.security.MessageDigest.isEqual(
                myproject.booking_tour.security.TokenHasher.sha256Hex(token == null ? "" : token.trim())
                        .getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                resetToken.getTokenHash().getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        if (!matches) {
            resetToken.setFailedAttempts(resetToken.getFailedAttempts() + 1);
            if (resetToken.getFailedAttempts() >= MAX_RESET_CODE_ATTEMPTS) {
                log.warn("Huy ma khoi phuc mat khau cua user {} sau {} lan nhap sai",
                        owner.getId(), resetToken.getFailedAttempts());
                tokenRepository.delete(resetToken);
            } else {
                tokenRepository.save(resetToken);
            }
            throw new BadRequestException(INVALID_RESET_CODE);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Reset tracking on success
        user.setForgotPasswordAttempts(0);
        user.setForgotPasswordBanUntil(null);
        user.setForgotPasswordLastAttempt(null);
        userRepository.save(user);

        // Delete token after successful use
        tokenRepository.delete(resetToken);

        // Doi mat khau thi moi phien cu deu phai chet. Neu ke tan cong dang giu
        // mot refresh token con han thi day la cach duy nhat cat duoc no - va
        // day cung la viec nguoi dung nghi rang minh dang lam khi doi mat khau.
        refreshTokenService.revokeAllSessions(user.getId(), RefreshToken.RevocationReason.PASSWORD_RESET);
    }

    /**
     * Dang xuat: thu hoi ca family refresh token.
     *
     * Access token dang cam KHONG bi vo hieu hoa ngay - no tu het han trong toi
     * da 15 phut. Doi lai he thong khong con phai tra danh sach den o moi
     * request. Dieu quan trong la ke nao cam duoc token cu cung khong the doi
     * lay token moi nua.
     */
    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeSession(refreshToken);
    }
}
