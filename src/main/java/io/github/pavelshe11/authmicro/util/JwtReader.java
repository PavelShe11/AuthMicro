package io.github.pavelshe11.authmicro.util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtReader {

    private final JwtDecoder jwtDecoder;

    public JwtReader(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public UUID extractAccountId(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return UUID.fromString(jwt.getClaimAsString("accountId"));
    }

    public boolean isTokenValid(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}