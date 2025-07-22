package io.github.pavelshe11.authmicro.api.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${resource-url}")
    private String resourceServerURL;

    @Value("${spring.security.oauth2.client.registration.keycloak.authorization-grant-type}")
    private String grantType;

}
