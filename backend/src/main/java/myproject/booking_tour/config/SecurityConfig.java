package myproject.booking_tour.config;

import lombok.RequiredArgsConstructor;
import myproject.booking_tour.security.CustomUserDetailsService;
import myproject.booking_tour.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final myproject.booking_tour.security.RestAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Thung rac cua trang quan tri - PHAI dung TRUOC dong
                // "GET /api/tours/**" ben duoi. Spring Security khop theo thu tu
                // khai bao, dong dau tien trung la thang; de sau thi permitAll
                // nuot mat va ai cung liet ke duoc tour da xoa kem gia, mo ta,
                // lich trinh. getDeletedTours() khong co chot nao ben trong.
                .requestMatchers(HttpMethod.GET, "/api/tours/trash").hasRole("ADMIN")

                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tours/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/accommodations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/utilities/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/payment/payos_transfer_handler").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/payment/payos_transfer_handler/verify").hasAnyRole("CUSTOMER", "ADMIN")
                
                // Admin specific endpoints
                .requestMatchers(HttpMethod.POST, "/api/tours/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tours/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tours/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.POST, "/api/accommodations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/accommodations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/accommodations/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.POST, "/api/utilities/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/utilities/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/utilities/**").hasRole("ADMIN")
                
                // User Profile Endpoints
                .requestMatchers(HttpMethod.GET, "/api/users/profile").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/profile/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                
                // Bookings Endpoints
                .requestMatchers(HttpMethod.GET, "/api/bookings/my-bookings").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/bookings").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/bookings/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.PUT, "/api/payments/*/status").hasRole("ADMIN")
                // Ghi nhan thanh toan thu cong (tien mat tai van phong) - chi
                // admin. Truoc day no roi vao dong "/api/payments/**" ben duoi,
                // nen bat cu khach hang nao cung tao duoc ban ghi payment tren
                // don cua nguoi khac va doc duoc so tien phai tra cua ho.
                .requestMatchers(HttpMethod.POST, "/api/payments").hasRole("ADMIN")
                .requestMatchers("/api/payments/**").hasAnyRole("CUSTOMER", "ADMIN")

                // Voucher Endpoints
                .requestMatchers(HttpMethod.GET, "/api/vouchers/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/vouchers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/vouchers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/vouchers/**").hasRole("ADMIN")

                // Admin Dashboard Endpoints
                .requestMatchers("/api/admin/dashboard/**").hasRole("ADMIN")

                // Contact Message Endpoints
                .requestMatchers(HttpMethod.POST, "/api/contacts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contacts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/contacts/**").hasRole("ADMIN")

                // Upload Endpoints
                .requestMatchers("/api/upload/**").hasAnyRole("CUSTOMER", "ADMIN")

                // Wishlist Endpoints - luon gan voi tai khoan dang dang nhap
                .requestMatchers("/api/wishlist/**").hasAnyRole("CUSTOMER", "ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            // 401 cho "chua dang nhap" thay vi 403 mac dinh. Frontend dua vao
            // dung ma nay de biet luc nao can goi /api/auth/refresh.
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        // Tách chuỗi URL bằng dấu phẩy để hỗ trợ nhiều domain
        configuration.setAllowedOrigins(java.util.Arrays.asList(frontendUrl.split(",")));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
