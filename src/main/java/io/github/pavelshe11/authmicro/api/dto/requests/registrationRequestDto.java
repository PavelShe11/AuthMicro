package io.github.pavelshe11.authmicro.api.dto.requests;

import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class registrationRequestDto {

    private String email;

    private Instant codeExpires;

    @AssertTrue
    private Boolean acceptedPrivacyPolicy;

    @AssertTrue
    private Boolean acceptedPersonalDataProcessing;
}
