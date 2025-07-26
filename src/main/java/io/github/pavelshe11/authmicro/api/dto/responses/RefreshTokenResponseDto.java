package io.github.pavelshe11.authmicro.api.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class RefreshTokenResponseDto {
    private String refreshToken;
    private Instant refreshTokenExpires;
    private String accessToken;
    private Instant accessTokenExpires;

}
