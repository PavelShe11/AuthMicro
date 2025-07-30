package io.github.pavelshe11.authmicro.services;


import io.github.pavelshe11.authmicro.api.grpc.client.AccountValidatorGrpc;
import io.github.pavelshe11.authmicro.api.grpc.client.RoleResolverGrpc;
import io.github.pavelshe11.authmicro.api.http.server.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.http.server.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.api.http.server.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import io.github.pavelshe11.authmicro.validators.LoginValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    private final LoginSessionRepository loginSessionRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final JwtUtil jwtUtil;
    private final AccountValidatorGrpc accountValidatorGrpc;
    private final RoleResolverGrpc roleResolverGrpc;
    private final LoginValidation loginValidator;

    public LoginResponseDto login(String email) {
        email = loginValidator.getTrimmedEmail(email);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse =
                accountValidatorGrpc.validateUserData(
                        Map.of(
                        "email", email,
                                "typeOfActivity", "login"
                )
        );

        loginValidator.validateUserDataOrThrow(accountValidatorResponse);

        String accountIdStr =accountValidatorResponse.getAccountId();

        if (accountIdStr.isBlank()) {
            String fakeCode = codeGeneratorService.codeGenerate();
            Instant fakeCodeExpires = codeGeneratorService.codeExpiresGenerate();
            return new LoginResponseDto(fakeCodeExpires, fakeCode);
        }

        UUID accountId = UUID.fromString(accountIdStr);

        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByAccountIdAndEmail(accountId, email);

        if (loginSessionOpt.isPresent()) {
            LoginSessionEntity session = loginSessionOpt.get();

            String newCode = codeGeneratorService.codeGenerate();
            Instant newCodeExpires = codeGeneratorService.codeExpiresGenerate();

            session.setCode(newCode);
            session.setCodeExpires(newCodeExpires);
            loginSessionRepository.save(session);

            return new LoginResponseDto(newCodeExpires, newCode);
        } else {
            String code = codeGeneratorService.codeGenerate();
            Instant codeExpires = codeGeneratorService.codeExpiresGenerate();
            LoginSessionEntity loginSession = LoginSessionEntity.builder()
                    .accountId(accountId)
                    .email(email)
                    .code(code)
                    .codeExpires(codeExpires)
                    .build();
            loginSessionRepository.save(loginSession);
            return new LoginResponseDto(codeExpires, code);
        }
    }

    public LoginConfirmResponseDto confirmLoginEmail(String email, String code) {
        email = loginValidator.getTrimmedEmail(email);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse =
                accountValidatorGrpc.validateUserData(
                        Map.of(
                                "email", email,
                                "typeOfActivity", "login"
                        )
                );

        loginValidator.validateUserDataOrThrow(accountValidatorResponse);

        String accountIdStr =accountValidatorResponse.getAccountId();

        if (accountIdStr.isBlank()) {
            throw new InvalidCodeException("error", "Неверный код подтверждения");
        }

        UUID accountId = UUID.fromString(accountIdStr);

        LoginSessionEntity session = loginValidator.getValidLoginSessionOrThrow(accountId, email);

        loginValidator.checkIfCodeIsValid(session, code);
        loginValidator.ensureCodeIsNotExpired(session);

        boolean isAdmin = roleResolverGrpc.isAdmin(accountId);

        String accessToken = jwtUtil.generateAccessToken(accountId, isAdmin);
        String refreshToken = jwtUtil.generateRefreshToken(accountId, isAdmin);

        Instant accessTokenExpires = jwtUtil.extractExpiration(accessToken);
        Instant refreshTokenExpires = jwtUtil.extractExpiration(refreshToken);

        loginSessionRepository.save(session);

        return LoginConfirmResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpires(accessTokenExpires)
                .refreshTokenExpires(refreshTokenExpires)
                .build();
    }
}