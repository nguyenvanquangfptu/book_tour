package myproject.booking_tour.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Doc access token tu header Authorization va dung no de xac thuc request.
 *
 * KHONG con tra danh sach den. Truoc day moi request da xac thuc phai them mot
 * query vao bang invalidated_tokens, doi lay viec logout vo hieu hoa token ngay
 * lap tuc - danh doi hop ly khi token song 24 gio. Voi 15 phut thi khong con
 * dang: logout gio thu hoi ca family refresh token (ke trom khong lam moi duoc
 * nua), con access token cu tu chet trong toi da 15 phut.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(SecurityConstants.AUTH_HEADER);
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Chu ky sai hoac token het han: di tiep khong xac thuc. Endpoint nao
            // can quyen se tra 401, frontend bat 401 do de goi /api/auth/refresh.
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
