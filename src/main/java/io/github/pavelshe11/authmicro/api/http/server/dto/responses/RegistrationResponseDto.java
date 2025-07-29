package io.github.pavelshe11.authmicro.api.http.server.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class RegistrationResponseDto {
    private Instant codeExpires;
    private String code;
}
