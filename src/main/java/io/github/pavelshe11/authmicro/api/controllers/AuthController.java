package io.github.pavelshe11.authmicro.api.controllers;

import io.github.pavelshe11.authmicro.api.dto.requests.*;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.RefreshTokenResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.services.LoginService;
import io.github.pavelshe11.authmicro.services.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;


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
        return registrationService.register(registrationRequest);
    }

    @PostMapping(AUTH + API_VERSION + REGISTRATION + CONFIRM)
    public ResponseEntity<Void> registrationConfirmEmail(
            @Valid @RequestBody RegistrationConfirmRequestDto registrationConfirmRequest) {
        return registrationService.confirmEmail(
                registrationConfirmRequest.getEmail(),
                registrationConfirmRequest.getCode());
    }

    @PostMapping(AUTH + API_VERSION + LOGIN + SEND_CODE)
    public LoginResponseDto sendLoginCode(
            @Valid @RequestBody LoginRequestDto loginRequest
    ) {
        // TODO: send email
        return loginService.login(loginRequest.getEmail());
    }

    @PostMapping(AUTH + API_VERSION + LOGIN + CONFIRM)
    public LoginConfirmResponseDto confirmLoginEmail(
            @Valid @RequestBody LoginConfirmRequestDto loginConfirmRequest
    ) {
        return loginService.confirmLoginEmail(
                loginConfirmRequest.getEmail(),
                loginConfirmRequest.getCode()
        );
    }

    @PostMapping(AUTH + API_VERSION + REFRESH_TOKEN)
    public RefreshTokenResponseDto refreshToken(
            @RequestBody RefreshTokenRequestDto refreshTokenRequest
    ) {
        return refreshTokenService.refreshTokens(
                refreshTokenRequest.getRefreshToken()
        );
    }
}

