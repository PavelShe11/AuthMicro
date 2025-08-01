package io.github.pavelshe11.authmicro.api.server.http.controllers;

import io.github.pavelshe11.authmicro.api.dto.requests.LoginConfirmRequestDto;
import io.github.pavelshe11.authmicro.api.dto.requests.LoginRequestDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.services.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/v1/login")
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/sendCodeEmail")
    public LoginResponseDto sendLoginCode(
            @Valid @RequestBody LoginRequestDto loginRequest
    ) {
        // TODO: send email
        return loginService.login(loginRequest.getEmail());
    }

    @PostMapping("/confirmEmail")
    public LoginConfirmResponseDto confirmLoginEmail(
            @Valid @RequestBody LoginConfirmRequestDto loginConfirmRequest,
            HttpServletRequest httpRequest
    ) {
        String ip;
        String header = httpRequest.getHeader("X-Forwarded-For");
        if (header != null && !header.isEmpty() && !"unknown".equalsIgnoreCase(header)) {
            header.split(",")[0].trim();
            ip = header;
        } else {
            ip = httpRequest.getRemoteAddr();
        }
        String userAgent = httpRequest.getHeader("User-Agent");
        return loginService.confirmLoginEmail(
                loginConfirmRequest.getEmail(),
                loginConfirmRequest.getCode(),
                ip, userAgent
        );
    }
}
