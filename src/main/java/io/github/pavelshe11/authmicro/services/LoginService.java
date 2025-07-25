package io.github.pavelshe11.authmicro.services;


import io.github.pavelshe11.authmicro.api.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.BadRequestException;
import io.github.pavelshe11.authmicro.api.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class LoginService {
    private final LoginSessionRepository loginSessionRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final JwtUtil jwtUtil;
    private final EmailValidatorGrpcService emailValidatorGrpcService;
    private final RoleResolverGrpcService roleResolverGrpcService;

    public LoginResponseDto login(String email) {
        if (email.trim().isEmpty()) {
            throw new BadRequestException("Поле Email не может быть пустым.");
        }

        String code = codeGeneratorService.codeGenerate();
        Instant codeExpires = codeGeneratorService.codeExpiresGenerate();

        Optional<String> accountIdOpt = emailValidatorGrpcService.getAccountIdIfExists(email);
        if (accountIdOpt.isEmpty()) {
            return new LoginResponseDto(codeExpires, code);
        }

        UUID accountId = UUID.fromString(accountIdOpt.get());

        LoginSessionEntity loginSession = LoginSessionEntity.builder()
                .accountId(accountId)
                .code(code)
                .codeExpires(codeExpires)
                .build();

        loginSessionRepository.save(loginSession);

        return new LoginResponseDto(codeExpires, code);
    }

    public LoginConfirmResponseDto confirmLoginEmail(String email, String code) {
        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByCode(code);

        if (loginSessionOpt.isEmpty()) {
            throw new InvalidCodeException("Неверный код подтверждения");
        }

        if (loginSessionOpt.get().getCodeExpires().isBefore(Instant.now())) {
            throw new CodeVerificationException("Код подтверждения истёк. Пожалуйста, запросите новый код и попробуйте снова.");
        }

        UUID accountId = loginSessionOpt.get().getAccountId();

        boolean isAdmin = roleResolverGrpcService.isAdmin(accountId);

        String accessToken = jwtUtil.generateAccessToken(accountId, isAdmin);
        String refreshToken = jwtUtil.generateRefreshToken(accountId, isAdmin);

        return LoginConfirmResponseDto.builder()
                .accessToken(accessToken)
                .requestToken(refreshToken)
                .build();
    }
}