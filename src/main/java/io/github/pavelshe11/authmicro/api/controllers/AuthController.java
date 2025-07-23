package io.github.pavelshe11.authmicro.api.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.github.pavelshe11.authmicro.api.constants.APIRoutes.*;

@RestController
public class AuthController {

    @GetMapping(AUTH + TEST + PUBLIC)
    public String publicEndpoint() {
        return "Доступ для всех";
    }

    @GetMapping(AUTH + TEST + PROTECTED)
    public String protectedEndpoint(@AuthenticationPrincipal Jwt jwt) {
        return "Аутентфикация: " + jwt.getSubject();
    }

    @GetMapping(AUTH + TEST + ROLES_LIST)
    public String roleInfo(@AuthenticationPrincipal Jwt jwt) {
        return "Список ролей: " + jwt.getClaimAsStringList("realm_access").toString();
    }
}
