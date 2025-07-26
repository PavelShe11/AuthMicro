package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.api.dto.responses.RefreshTokenResponseDto;
import io.github.pavelshe11.authmicro.store.entities.RefreshTokenSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.RefreshTokenSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import io.github.pavelshe11.authmicro.validators.RefreshTokenValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenValidation refreshTokenValidator;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    public RefreshTokenResponseDto refreshTokens(String refreshToken, String ip, String userAgent) {

        Jwt decodedToken = refreshTokenValidator.getDecodedTokenOrThrow(refreshToken);

        refreshTokenValidator.checkIfTokenValidOrThrow(decodedToken);
        refreshTokenValidator.checkIfTokenNotExpiredOrThrow(decodedToken);

        String accountIdStr = decodedToken.getClaimAsString("accountId");
        List<String> roles = decodedToken.getClaimAsStringList("roles");
        boolean isAdmin = roles.contains("admin");

        UUID accountId =  UUID.fromString(accountIdStr);

        String newAccessToken = jwtUtil.generateAccessToken(accountId, isAdmin);
        String newRefreshToken = jwtUtil.generateAccessToken(accountId, isAdmin);

        Instant accessTokenExpires = jwtUtil.extractExpiration(newAccessToken);
        Instant refreshTokenExpires = jwtUtil.extractExpiration(newRefreshToken);

        RefreshTokenSessionEntity session = RefreshTokenSessionEntity.builder()
                .accountId(accountId)
                .refreshToken(newRefreshToken)
                .userAgent(userAgent)
                .ip(ip)
                .accessTokenExpires(accessTokenExpires)
                .refreshTokenExpires(refreshTokenExpires)
                .build();

        refreshTokenSessionRepository.save(session);


        return new RefreshTokenResponseDto(newRefreshToken, refreshTokenExpires, newAccessToken, accessTokenExpires);
    }
}
