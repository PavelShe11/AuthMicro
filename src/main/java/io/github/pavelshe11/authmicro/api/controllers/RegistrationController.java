package io.github.pavelshe11.authmicro.api.controllers;

import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationConfirmRequestDto;
import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationRequestDto;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.services.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/v1/registration")
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping()
    public RegistrationResponseDto sendRegistrationCode(@Valid @RequestBody RegistrationRequestDto registrationRequest) {
        // TODO: send email
        return registrationService.register(registrationRequest);
    }

    @PostMapping("/confirmEmail")
    public ResponseEntity<Void> registrationConfirmEmail(
            @Valid @RequestBody RegistrationConfirmRequestDto registrationConfirmRequest) {
        return registrationService.confirmEmail(
                registrationConfirmRequest);
    }
}
