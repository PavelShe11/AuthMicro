package io.github.pavelshe11.authmicro.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final JwtDecoder jwtDecoder;

    public JwtUtil(SecretKey key, JwtDecoder jwtDecoder) {
        this.key = key;
        this.jwtDecoder = jwtDecoder;
    }

    public String generateAccessToken(UUID accountId, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("accountId", accountId.toString());
        claims.put("roles", List.of(isAdmin ? "admin" : "user"));
        return createToken(claims, 5 * 60 * 1000L); // 5 min
    }

    public String generateRefreshToken(UUID accountId, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("accountId", accountId.toString());
        claims.put("roles", List.of(isAdmin ? "admin" : "user"));
        return createToken(claims, 5 * 24 * 60 * 60 * 1000L);
    }

    private String createToken(Map<String, Object> claims, Long time) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("micro-auth")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + time)) // 5 days expiration
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Instant extractExpiration(String token) {
        Jwt decodedJwt = jwtDecoder.decode(token);
        return decodedJwt.getExpiresAt();
    }
}
