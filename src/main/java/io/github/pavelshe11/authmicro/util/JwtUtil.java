package io.github.pavelshe11.authmicro.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(SecretKey key) {
        this.key = key;
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
                .signWith(key)
                .compact();
    }
}
//    private Claims extractAllClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    public Date extractExpirationTime(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    private Boolean isTokenExpired(String token) {
//        return extractExpirationTime(token).before(new Date());
//    }
//
//    public Boolean validateToken(String token) {
//       return (!isTokenExpired(token));
//    }
//}
