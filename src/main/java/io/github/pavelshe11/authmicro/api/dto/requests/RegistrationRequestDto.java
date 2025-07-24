package io.github.pavelshe11.authmicro.api.dto.requests;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class RegistrationRequestDto {

    @NotBlank(message = "Поле Email не может быть пустым.")
    private String email;

    @AssertTrue(message = "Не принято пользовательское соглашение.")
    private Boolean acceptedPrivacyPolicy;

    @AssertTrue(message = "Не принято пользовательское соглашение.")
    private Boolean acceptedPersonalDataProcessing;
}
