package io.github.pavelshe11.authmicro.api.dto.requests;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LoginConfirmRequestDto {

    @Email(message = "Некорректный формат Email.")
    private String email;

    private String code;
}
