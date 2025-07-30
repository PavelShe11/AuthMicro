package io.github.pavelshe11.authmicro.api.dto.requests;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class RegistrationConfirmRequestDto {

    @Email(message = "Некорректный формат Email.")
    private String email;

    private String code;

    private Boolean acceptedPrivacyPolicy;

    private Boolean acceptedPersonalDataProcessing;

}
