package io.github.pavelshe11.authmicro.services;


import io.github.pavelshe11.authmicro.api.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.BadRequestException;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
@RequiredArgsConstructor
public class LoginService {
    private final CodeGeneratorService codeGeneratorService;
    private final JwtUtil jwtUtil;

    public LoginResponseDto login(String email) {
        if (email.trim().isEmpty()) {
            throw new BadRequestException("Поле Email не может быть пустым.");
        }
        // TODO: стучим к networking по grpc для проверки и получения accountID

        String code = codeGeneratorService.codeGenerate();
        Instant codeExpires = codeGeneratorService.codeExpiresGenerate();

//        LoginSessionEntity loginSession = LoginSessionRepository
//                .findByAccountId()


        return null;
    }

    public LoginConfirmResponseDto confirmLoginEmail() {
        String accessToken = jwtUtil.generateAccessToken();
        String refreshToken = jwtUtil.generateRefreshToken();

        return LoginConfirmResponseDto.builder()
                .accessToken(accessToken)
                .requestToken(refreshToken)
                .build();
    }
}