package myproject.booking_tour.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * SHA-256 cua token, dang hex 64 ky tu - dung lam khoa trong bang
     * invalidated_tokens thay vi luu chinh chuoi JWT.
     *
     * Ba ly do:
     *   1. Do dai co dinh 64 ky tu. Cot cu la VARCHAR(255) trong khi entity khai
     *      1024; voi username dai (cot username cho phep 100 ky tu) token that
     *      co the vuot 255 -> INSERT that bai luc logout. Bam xong thi khong
     *      bao gio con phu thuoc vao do dai token.
     *   2. Index nho va nhanh hon nhieu so voi chuoi vai tram ky tu.
     *   3. Neu lo ban sao database, ke tan cong khong lay duoc token con hieu
     *      luc - vi ham bam mot chieu.
     *
     * LUU Y: ham nay phai duoc dung o CA HAI noi - luc ghi (logout) va luc kiem
     * tra (JwtAuthenticationFilter). Neu chi sua mot ben, danh sach den se im
     * lang mat tac dung.
     */
    public String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 la thuat toan bat buoc co trong moi JVM
            throw new IllegalStateException("Khong tim thay thuat toan SHA-256", e);
        }
    }

    public Boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
