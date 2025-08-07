package io.github.pavelshe11.authmicro.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.sql.Timestamp;
import java.util.*;

@Component
public class JwtUtil {

    @Value("${jwt.access-token.lifetime}")
    private long accessTokenLifetime;


    @Value("${jwt.refresh-token.lifetime}")
    private long refreshTokenLifetime;

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
        return createToken(claims, accessTokenLifetime);
    }

    public String generateRefreshToken(UUID accountId, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("accountId", accountId.toString());
        claims.put("roles", List.of(isAdmin ? "admin" : "user"));
        return createToken(claims, refreshTokenLifetime);
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

    public Timestamp extractExpiration(String token) {
        Jwt decodedJwt = jwtDecoder.decode(token);
        return Timestamp.from(Objects.requireNonNull(decodedJwt.getExpiresAt()));
    }
}
