package io.github.pavelshe11.authmicro.api.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RefreshTokenResponseDto {
    private String refreshToken;
    private long refreshTokenExpires;
    private String accessToken;
    private long accessTokenExpires;

}
