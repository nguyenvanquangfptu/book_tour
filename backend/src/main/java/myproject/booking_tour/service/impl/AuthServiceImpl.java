package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.LoginRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.response.AuthResponse;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.exception.UnauthorizedException;
import myproject.booking_tour.repository.RoleRepository;
import myproject.booking_tour.repository.UserRepository;
import myproject.booking_tour.service.AuthService;
import myproject.booking_tour.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
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

        // Generate real JWT token
        String token = jwtService.generateToken(savedUser.getUsername());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                role.getName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password!"));

        // Check password matching
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password!");
        }

        // Generate real JWT token
        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole() != null ? user.getRole().getName() : "CUSTOMER"
        );
    }

    @org.springframework.beans.factory.annotation.Value("${google.client.id:YOUR_GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(String idTokenString) {
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

                User user = userRepository.findByEmail(email).orElse(null);

                if (user == null) {
                    Role role = roleRepository.findByName("CUSTOMER")
                            .orElseGet(() -> {
                                Role newRole = new Role();
                                newRole.setName("CUSTOMER");
                                return roleRepository.save(newRole);
                            });

                    user = new User();
                    user.setUsername(email); // Use email as username for Google login
                    user.setEmail(email);
                    user.setFullName(name);
                    user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString())); // Random password
                    user.setRole(role);
                    user = userRepository.save(user);
                }

                String token = jwtService.generateToken(user.getUsername());
                return new AuthResponse(
                        token,
                        user.getId(),
                        user.getUsername(),
                        user.getRole() != null ? user.getRole().getName() : "CUSTOMER"
                );
            } else {
                throw new UnauthorizedException("Invalid ID token.");
            }
        } catch (Exception e) {
            throw new UnauthorizedException("Failed to verify Google Token: " + e.getMessage());
        }
    }

    @Autowired
    private myproject.booking_tour.repository.PasswordResetTokenRepository tokenRepository;

    @Autowired
    private myproject.booking_tour.service.EmailService emailService;

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new myproject.booking_tour.exception.ResourceNotFoundException("User not found with email: " + email));

        String tokenString = java.util.UUID.randomUUID().toString();
        myproject.booking_tour.entity.PasswordResetToken resetToken = new myproject.booking_tour.entity.PasswordResetToken(
                tokenString, user, java.time.LocalDateTime.now().plusMinutes(15)
        );
        tokenRepository.save(resetToken);

        try {
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("resetToken", tokenString);
            emailService.sendMessageUsingThymeleafTemplate(user.getEmail(), "Khôi phục mật khẩu", "reset-password", model);
        } catch (Exception e) {
            System.err.println("Failed to send reset password email: " + e.getMessage());
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
        userRepository.save(user);

        // Delete token after successful use
        tokenRepository.delete(resetToken);
    }
}
