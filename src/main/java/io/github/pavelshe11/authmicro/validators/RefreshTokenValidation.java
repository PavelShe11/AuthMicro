package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RefreshTokenValidation {
    private final JwtDecoder jwtDecoder;

    public Jwt getDecodedTokenOrThrow(String refreshToken) {
        Jwt decodedToken;
        try {
            return jwtDecoder.decode(refreshToken);
        } catch (JwtException e) {
            throw new InvalidTokenException("error", "Невалидный токен.");
        }
    }

    public void checkIfTokenValidOrThrow(Jwt decodedToken) {
        if (!"refresh".equals(decodedToken.getClaimAsString("type"))) {
            throw new InvalidTokenException("error", "Невалидный токен.");
        }
    }

    public void checkIfTokenNotExpiredOrThrow(Jwt decodedToken) {
        if (decodedToken.getExpiresAt() == null || decodedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new CodeVerificationException("error", "Ошибка времени действия кода.");
        }
    }
}
