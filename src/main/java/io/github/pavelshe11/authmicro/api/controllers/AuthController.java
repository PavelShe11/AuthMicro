package io.github.pavelshe11.authmicro.api.controllers;

import io.github.pavelshe11.authmicro.api.dto.requests.LoginConfirmRequestDto;
import io.github.pavelshe11.authmicro.api.dto.requests.LoginRequestDto;
import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationConfirmRequestDto;
import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationRequestDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.services.LoginService;
import io.github.pavelshe11.authmicro.services.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static io.github.pavelshe11.authmicro.api.constants.APIRoutes.*;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;


    @PostMapping("/test/user")
    public String userAuthorisation() {
        return "This is user!";
    }


    @PostMapping("/test/public")
    public String withoutAuthorisation() {
        return "This is public!";
    }


    @PostMapping("/test/admin")
    public String adminAuthorisation() {
        return "This is admin!";
    }




    @PostMapping(AUTH + API_VERSION + REGISTRATION)
    public RegistrationResponseDto sendRegistrationCode(@Valid @RequestBody RegistrationRequestDto registrationRequest) {
        // TODO: send email
        return registrationService.register(registrationRequest.getEmail());
    }

    @PostMapping(AUTH + API_VERSION + REGISTRATION + CONFIRM)
    public ResponseEntity<Void> registrationConfirmEmail(
            @RequestBody RegistrationConfirmRequestDto registrationConfirmRequest) {
        return registrationService.confirmEmail(
                registrationConfirmRequest.getRegistrationId(),
                registrationConfirmRequest.getEmail(),
                registrationConfirmRequest.getCode());
    }

    @PostMapping(AUTH + API_VERSION + LOGIN + SEND_CODE)
    public LoginResponseDto sendLoginCode(
            @RequestBody LoginRequestDto loginRequest
    ) {
        // TODO: send email
        return loginService.login(loginRequest.getEmail());
    }

    @PostMapping(AUTH + API_VERSION + LOGIN + CONFIRM)
    public LoginConfirmResponseDto confirmLoginEmail(
            @RequestBody LoginConfirmRequestDto loginConfirmRequest
    ) {
        return loginService.confirmLoginEmail(
                loginConfirmRequest.getEmail(),
                loginConfirmRequest.getCode()
        );
    }

    @PostMapping(AUTH + API_VERSION + REFRESH_TOKEN)
    public ResponseEntity<Void> refreshToken() {
        return ResponseEntity.ok().build();
    }
}

