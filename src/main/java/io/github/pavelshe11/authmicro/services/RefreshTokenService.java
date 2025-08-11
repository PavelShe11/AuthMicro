package io.github.pavelshe11.authmicro.services;

import com.google.protobuf.Value;
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
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenValidation refreshTokenValidator;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final GetAccountInfoGrpc getAccountInfoGrpc;
    private final SessionCleanerService sessionCleanerService;

    @Transactional
    public RefreshTokenResponseDto refreshTokens(String refreshToken) {

        refreshTokenValidator.checkIfTokenExistsOrThrow(refreshToken);

        Jwt decodedToken = refreshTokenValidator.getDecodedTokenOrThrow(refreshToken);


        refreshTokenValidator.checkIfTokenValidOrThrow(decodedToken);
        refreshTokenValidator.checkIfTokenNotExpiredOrThrow(decodedToken);

        String accountIdStr = decodedToken.getClaimAsString("accountId");
        UUID accountId = UUID.fromString(accountIdStr);

        var accountInfoOpt = getAccountInfoGrpc.getAccountInfoById(accountIdStr);

        if (accountInfoOpt.isEmpty()) {
            throw new InvalidTokenException();
        }
        var accountInfo = accountInfoOpt.get();
        Map<String, Value> userData = accountInfo.getUserDataMap();

        String role = userData.getOrDefault("role", Value.newBuilder().setStringValue("").build()).getStringValue();
        boolean isAdmin = "admin".equalsIgnoreCase(role);

        String currentIp = userData.getOrDefault("ip", Value.newBuilder().setStringValue("").build()).getStringValue();

        String newAccessToken = jwtUtil.generateAccessToken(accountId, isAdmin);
        String newRefreshToken = jwtUtil.generateRefreshToken(accountId);

        Timestamp accessTokenExpires = jwtUtil.extractExpiration(newAccessToken);
        Timestamp refreshTokenExpires = jwtUtil.extractExpiration(newRefreshToken);

        RefreshTokenSessionEntity oldSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException());

        RefreshTokenSessionEntity session = RefreshTokenSessionEntity.builder()
                .accountId(accountId)
                .refreshToken(newRefreshToken)
                .ip(currentIp)
                .userAgent(oldSession.getUserAgent())
                .expiresAt(refreshTokenExpires)
                .build();

        sessionCleanerService.cleanRefreshTokenSession(oldSession);

        refreshTokenSessionRepository.save(session);


        return new RefreshTokenResponseDto(newRefreshToken,
                refreshTokenExpires.getTime(), newAccessToken, accessTokenExpires.getTime());
    }
}
