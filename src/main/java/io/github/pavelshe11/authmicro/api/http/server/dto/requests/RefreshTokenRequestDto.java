package io.github.pavelshe11.authmicro.api.http.server.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RefreshTokenRequestDto {
    private String refreshToken;
}
