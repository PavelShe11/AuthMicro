package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.exceptions.CodeIsNotExpiredException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidTokenException;
import io.github.pavelshe11.authmicro.store.repositories.RefreshTokenSessionRepository;
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
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    public Jwt getDecodedTokenOrThrow(String refreshToken) {
        try {
            return jwtDecoder.decode(refreshToken);
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    public void checkIfTokenValidOrThrow(Jwt decodedToken) {
        if (!"refresh".equals(decodedToken.getClaimAsString("type"))) {
            throw new InvalidTokenException();
        }
    }

    public void checkIfTokenNotExpiredOrThrow(Jwt decodedToken) {
        if (decodedToken.getExpiresAt() == null || decodedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new CodeIsNotExpiredException();
        }
    }

    public void checkIfTokenExistsOrThrow(String refreshToken) {
        if (!(refreshTokenSessionRepository.existsByRefreshToken(refreshToken))) {
            throw new InvalidTokenException();
        }
    }
}
