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

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tours/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/accommodations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/utilities/**").permitAll()
                
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
                
                // Customer / authenticated bookings
                .requestMatchers("/api/bookings/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/payments/**").hasAnyRole("CUSTOMER", "ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
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
        configuration.setAllowedOrigins(java.util.List.of(frontendUrl));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
