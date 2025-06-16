package org.example.utils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.example.domain.User;
import java.security.Key;
import java.util.Date;

/**
 * Service class for handling JWT operations such as token generation, validation, and extraction of user information.
 * Uses HMAC SHA-256 algorithm for signing tokens.
 */
@Service
public class JwtService {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 6; // 6h
    private final Key key;

    /**
     * Constructor that initializes the JWT signing key from application properties.
     *
     * @param secretKey The secret key used for signing JWT tokens, injected from application properties.
     */
    public JwtService(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Generates a JWT token for the given user.
     *
     * @param user The user for whom the token is generated.
     * @return A signed JWT token containing the user's username and ID.
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the given JWT token.
     *
     * @param token The JWT token to validate.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the username from the given JWT token.
     *
     * @param token The JWT token from which to extract the username.
     * @return The username contained in the token.
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Extracts the user ID from the given JWT token.
     *
     * @param token The JWT token from which to extract the user ID.
     * @return The user ID contained in the token.
     */
    public Long extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }
}
