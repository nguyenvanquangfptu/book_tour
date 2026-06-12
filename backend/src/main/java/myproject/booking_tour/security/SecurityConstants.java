package myproject.booking_tour.security;

public class SecurityConstants {
    // Secret key must be at least 256 bits (32 bytes) for HS256 algorithm.
    public static final String SECRET_KEY = "mySecretKeyForJWTAuthSystemBookingTourProjectSystemSecretKey";
    public static final long TOKEN_EXPIRATION = 86400000; // 1 day in milliseconds
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
}
