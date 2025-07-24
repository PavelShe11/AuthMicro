package io.github.pavelshe11.authmicro.api.dto.requests;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class RegistrationConfirmRequestDto {

    private UUID registrationId;

    @NotBlank(message = "Поле Email не может быть пустым.")
    private String email;

    private String code;

}
