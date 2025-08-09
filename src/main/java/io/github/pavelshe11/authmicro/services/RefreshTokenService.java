package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.api.client.grpc.GetAccountInfoGrpc;
import io.github.pavelshe11.authmicro.api.dto.responses.RefreshTokenResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidTokenException;
import io.github.pavelshe11.authmicro.store.entities.RefreshTokenSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.RefreshTokenSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import io.github.pavelshe11.authmicro.validators.RefreshTokenValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenValidation refreshTokenValidator;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final GetAccountInfoGrpc getAccountInfoGrpc;

    @Transactional
    public RefreshTokenResponseDto refreshTokens(String refreshToken) {

        refreshTokenValidator.checkIfTokenExistsOrThrow(refreshToken);

        Jwt decodedToken = refreshTokenValidator.getDecodedTokenOrThrow(refreshToken);


        refreshTokenValidator.checkIfTokenValidOrThrow(decodedToken);
        refreshTokenValidator.checkIfTokenNotExpiredOrThrow(decodedToken);

        String accountIdStr = decodedToken.getClaimAsString("accountId");
        List<String> roles = decodedToken.getClaimAsStringList("roles");
        boolean isAdmin = roles.contains("admin");

        boolean isAccountExists = getAccountInfoGrpc.checkIfAccountExistsById(accountIdStr);
        if (!isAccountExists) {
            throw new InvalidTokenException();
        }

        UUID accountId = UUID.fromString(accountIdStr);

        String newAccessToken = jwtUtil.generateAccessToken(accountId, isAdmin);
        String newRefreshToken = jwtUtil.generateAccessToken(accountId, isAdmin);

        Timestamp accessTokenExpires = jwtUtil.extractExpiration(newAccessToken);
        Timestamp refreshTokenExpires = jwtUtil.extractExpiration(newRefreshToken);

        RefreshTokenSessionEntity oldSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException());

        RefreshTokenSessionEntity session = RefreshTokenSessionEntity.builder()
                .accountId(accountId)
                .refreshToken(newRefreshToken)
                .ip(oldSession.getIp())
                .userAgent(oldSession.getUserAgent())
                .expiresAt(refreshTokenExpires)
                .build();

        refreshTokenSessionRepository.delete(oldSession);

        refreshTokenSessionRepository.save(session);


        return new RefreshTokenResponseDto(newRefreshToken,
                refreshTokenExpires.getTime(), newAccessToken, accessTokenExpires.getTime());
    }
}
