package myproject.booking_tour.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isAdmin_ShouldReturnTrue_WhenUserIsAdmin() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityUtil.isAdmin()).isTrue();
    }

    @Test
    void isAdmin_ShouldReturnFalse_WhenUserIsNotAdmin() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityUtil.isAdmin()).isFalse();
    }

    @Test
    void isAdmin_ShouldReturnFalse_WhenNotAuthenticated() {
        assertThat(SecurityUtil.isAdmin()).isFalse();
    }
}
