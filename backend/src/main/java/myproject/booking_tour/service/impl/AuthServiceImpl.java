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
        try {
            com.google.api.client.http.HttpTransport transport = new com.google.api.client.http.javanet.NetHttpTransport();
            com.google.api.client.json.JsonFactory jsonFactory = new com.google.api.client.json.gson.GsonFactory();
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier = new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String name = (String) payload.get("name");
                // String pictureUrl = (String) payload.get("picture");

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
            } else {
                throw new UnauthorizedException("Invalid ID token.");
            }
        } catch (Exception e) {
            System.err.println("Google Login Error:");
            e.printStackTrace();
            throw new UnauthorizedException("Failed to verify Google Token: " + e.getMessage());
        }
    }



    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new myproject.booking_tour.exception.ResourceNotFoundException("User not found with email: " + email));

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Check Ban
        if (user.getForgotPasswordBanUntil() != null && user.getForgotPasswordBanUntil().isAfter(now)) {
            long hours = java.time.Duration.between(now, user.getForgotPasswordBanUntil()).toHours();
            throw new BadRequestException("Bạn đã yêu cầu quá 3 lần. Vui lòng thử lại sau " + (hours > 0 ? hours : 1) + " giờ.");
        }

        // Reset if ban expired
        if (user.getForgotPasswordBanUntil() != null && user.getForgotPasswordBanUntil().isBefore(now)) {
            user.setForgotPasswordAttempts(0);
            user.setForgotPasswordBanUntil(null);
        }

        // Check Cooldown
        if (user.getForgotPasswordLastAttempt() != null && java.time.Duration.between(user.getForgotPasswordLastAttempt(), now).toMinutes() < 1) {
            throw new BadRequestException("Vui lòng đợi 1 phút trước khi yêu cầu lại mã mới.");
        }

        // Update tracking
        int attempts = user.getForgotPasswordAttempts() == null ? 0 : user.getForgotPasswordAttempts();
        user.setForgotPasswordAttempts(attempts + 1);
        user.setForgotPasswordLastAttempt(now);

        if (user.getForgotPasswordAttempts() >= 3) {
            user.setForgotPasswordBanUntil(now.plusHours(5));
        }

        userRepository.save(user);

        // Delete old tokens
        tokenRepository.deleteByUser(user);

        // Generate 6 digit OTP
        String tokenString = String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
        myproject.booking_tour.entity.PasswordResetToken resetToken = new myproject.booking_tour.entity.PasswordResetToken(
                tokenString, user, now.plusMinutes(5)
        );
        tokenRepository.save(resetToken);

        log.info("=================================================");
        log.info("MÃ OTP KHÔI PHỤC MẬT KHẨU CHO EMAIL {}: {}", email, tokenString);
        log.info("=================================================");

        try {
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("resetToken", tokenString);
            emailService.sendMessageUsingThymeleafTemplate(user.getEmail(), "Khôi phục mật khẩu", "reset-password", model);
        } catch (Exception e) {
            log.error("Failed to send reset password email: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        myproject.booking_tour.entity.PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid token!"));

        if (resetToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new BadRequestException("Token has expired!");
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
