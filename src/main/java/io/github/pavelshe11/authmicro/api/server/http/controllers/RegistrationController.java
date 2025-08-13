package io.github.pavelshe11.authmicro.api.server.http.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.services.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
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
    public RegistrationResponseDto sendRegistrationCode(@RequestBody JsonNode registrationRequest) {
        // TODO: send email
        return registrationService.register(registrationRequest);
    }

    @PostMapping("/confirmEmail")
    public ResponseEntity<Void> registrationConfirmEmail(
            @RequestBody JsonNode registrationConfirmRequest,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        registrationService.confirmEmail(
                registrationConfirmRequest, ip);
        return ResponseEntity.ok().build();
    }
}
