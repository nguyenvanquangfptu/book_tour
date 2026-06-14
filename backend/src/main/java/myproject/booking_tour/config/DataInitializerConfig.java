package myproject.booking_tour.config;

import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.repository.RoleRepository;
import myproject.booking_tour.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import myproject.booking_tour.entity.Accommodation;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.repository.AccommodationRepository;
import myproject.booking_tour.repository.TourRepository;
import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializerConfig {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, TourRepository tourRepository, AccommodationRepository accommodationRepository) {
        return args -> {
            // Seed default CUSTOMER role
            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("CUSTOMER");
                        return roleRepository.save(role);
                    });

            // Seed default ADMIN role
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ADMIN");
                        return roleRepository.save(role);
                    });

            // Seed default ADMIN account
            if (!userRepository.existsByUsername(adminUsername)) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setEmail(adminEmail);
                admin.setFullName("System Administrator");
                admin.setPhone("0123456789");
                admin.setRole(adminRole);
                userRepository.save(admin);
            }

            // Seed Tours if empty is handled by SQL script
        };
    }
}
