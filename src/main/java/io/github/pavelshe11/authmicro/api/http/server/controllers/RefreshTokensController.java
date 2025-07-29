package io.github.pavelshe11.authmicro.api.http.server.controllers;

import io.github.pavelshe11.authmicro.api.http.server.dto.requests.RefreshTokenRequestDto;
import io.github.pavelshe11.authmicro.api.http.server.dto.responses.RefreshTokenResponseDto;
import io.github.pavelshe11.authmicro.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
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
            @RequestBody RefreshTokenRequestDto refreshTokenRequest,
            HttpServletRequest httpRequest
    ) {
        String ip;
        String header = httpRequest.getHeader("X-Forwarded-For");
        if (header != null && !header.isEmpty() && !"unknown".equalsIgnoreCase(header)) {
            header.split(",")[0].trim();
            ip = header;
        } else {
            ip = httpRequest.getRemoteAddr();
        }
        String userAgent = httpRequest.getHeader("User-Agent");

        return refreshTokenService.refreshTokens(refreshTokenRequest.getRefreshToken(),
                ip,
                userAgent);

    }
}
