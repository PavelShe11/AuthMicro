package io.github.pavelshe11.authmicro.services;


import io.github.pavelshe11.authmicro.api.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import io.github.pavelshe11.authmicro.validators.LoginValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class LoginService {
    private final PasswordEncoder passwordEncoder;
    private final LoginSessionRepository loginSessionRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final JwtUtil jwtUtil;
    private final EmailValidatorGrpcService emailValidatorGrpcService;
    private final RoleResolverGrpcService roleResolverGrpcService;
    private final LoginValidation loginValidator;

    public LoginResponseDto login(String email) {
        email = loginValidator.validateAndTrimEmail(email);

        Optional<String> accountIdOpt = emailValidatorGrpcService.getAccountIdIfExists(email);

        UUID accountId = UUID.fromString(accountIdOpt.get());

        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByAccountIdAndEmail(accountId, email);

        if (loginSessionOpt.isPresent()) {
            LoginSessionEntity session = loginSessionOpt.get();

            if (session.getCodeExpires().isAfter(Instant.now())) {
                return new LoginResponseDto(session.getCodeExpires(), session.getCode());
            }

            String newCode = codeGeneratorService.codeGenerate();
            Instant newCodeExpires = codeGeneratorService.codeExpiresGenerate();

            session.setCode(passwordEncoder.encode(newCode));
            session.setCodeExpires(newCodeExpires);
            loginSessionRepository.save(session);

            return new LoginResponseDto(newCodeExpires, newCode);
        } else {
            String code = codeGeneratorService.codeGenerate();
            Instant codeExpires = codeGeneratorService.codeExpiresGenerate();
            LoginSessionEntity loginSession = LoginSessionEntity.builder()
                    .accountId(accountId)
                    .email(email)
                    .code(passwordEncoder.encode(code))
                    .codeExpires(codeExpires)
                    .build();
            loginSessionRepository.save(loginSession);
            return new LoginResponseDto(codeExpires, code);
        }
    }

    public LoginConfirmResponseDto confirmLoginEmail(String email, String code) {
        email = loginValidator.validateAndTrimEmail(email);

        UUID accountId = loginValidator.getAccountIdByEmailOrThrow(email);

        LoginSessionEntity session = loginValidator.getValidLoginSessionOrThrow(accountId, email);

        loginValidator.checkIfCodeIsValid(session, code, passwordEncoder);
        loginValidator.ensureCodeIsNotExpired(session);

        boolean isAdmin = roleResolverGrpcService.isAdmin(accountId);

        String accessToken = jwtUtil.generateAccessToken(accountId, isAdmin);
        String refreshToken = jwtUtil.generateRefreshToken(accountId, isAdmin);

        loginSessionRepository.delete(session);

        return LoginConfirmResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}