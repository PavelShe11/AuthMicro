package io.github.pavelshe11.authmicro.api.server.http.controllers;

import io.github.pavelshe11.authmicro.api.dto.requests.RefreshTokenRequestDto;
import io.github.pavelshe11.authmicro.api.dto.responses.RefreshTokenResponseDto;
import io.github.pavelshe11.authmicro.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/v1")
public class RefreshTokensController {
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refreshToken")
    public RefreshTokenResponseDto refreshToken(
            @RequestBody RefreshTokenRequestDto refreshTokenRequest
    ) {
        return refreshTokenService.refreshTokens(refreshTokenRequest.getRefreshToken());

    }
}
