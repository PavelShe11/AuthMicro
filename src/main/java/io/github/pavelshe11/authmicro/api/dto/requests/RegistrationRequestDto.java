package io.github.pavelshe11.authmicro.api.dto.requests;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class RegistrationRequestDto {

    @Email(message = "Некорректный формат Email.")
    private String email;

    private Boolean acceptedPrivacyPolicy;

    private Boolean acceptedPersonalDataProcessing;
}
