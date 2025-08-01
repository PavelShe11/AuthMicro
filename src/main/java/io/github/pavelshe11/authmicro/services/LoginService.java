package io.github.pavelshe11.authmicro.services;


import io.github.pavelshe11.authmicro.api.client.grpc.AccountValidatorGrpc;
import io.github.pavelshe11.authmicro.api.client.grpc.GetAccountInfoGrpc;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.grpc.getAccountInfoProto;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.entities.RefreshTokenSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import io.github.pavelshe11.authmicro.store.repositories.RefreshTokenSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import io.github.pavelshe11.authmicro.validators.LoginValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;


@Service
@RequiredArgsConstructor
public class LoginService {
    private final LoginSessionRepository loginSessionRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final JwtUtil jwtUtil;
    private final GetAccountInfoGrpc getAccountInfoGrpc;
    private final LoginValidation loginValidator;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    public LoginResponseDto login(String email) {

        email = loginValidator.ValidateAndGetTrimmedEmail(email);

        Optional<getAccountInfoProto.GetAccountInfoResponse> accountInfoOpt =
                getAccountInfoGrpc.getAccountInfo(email);

        if (accountInfoOpt.isEmpty()) {
            return fakeLoginSessionCreateAndSave(email);
        }
        getAccountInfoProto.GetAccountInfoResponse response = accountInfoOpt.get();

        return validLoginSessionCreateAndSave(response, email);

    }

    public LoginConfirmResponseDto confirmLoginEmail(String email, String code, String ip, String userAgent) {
        email = loginValidator.ValidateAndGetTrimmedEmail(email);
        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByEmail(email);

        if (loginSessionOpt.isEmpty()) {
            throw new ServerAnswerException("Сервер не отвечает.");
        }
        LoginSessionEntity loginSession =loginSessionOpt.get();

            if (loginSession.getAccountId() == null) {
                throw new InvalidCodeException("error", "Неверный код подтверждения.");
            }

            loginValidator.checkIfCodeIsValid(loginSession, code);
            loginValidator.ensureCodeIsNotExpired(loginSession);

            Optional<getAccountInfoProto.GetAccountInfoResponse> accountInfoOpt =
                    getAccountInfoGrpc.getAccountInfo(email);

        if (accountInfoOpt.isEmpty()) {
            throw new ServerAnswerException("Сервер не отвечает.");
        }

        getAccountInfoProto.GetAccountInfoResponse response = accountInfoOpt.get();

        if (!response.getAccountId().equals(loginSession.getAccountId().toString())) {
            throw new ServerAnswerException("Сервер не отвечает.");
        }

        UUID accountId = UUID.fromString(response.getAccountId());
        boolean isAdmin = response.getRole().equals("admin");

        String accessToken = jwtUtil.generateAccessToken(accountId, isAdmin);
        String refreshToken = jwtUtil.generateRefreshToken(accountId, isAdmin);

        Timestamp accessTokenExpires = jwtUtil.extractExpiration(accessToken);
        Timestamp refreshTokenExpires = jwtUtil.extractExpiration(refreshToken);

        loginSessionRepository.save(loginSession);

        RefreshTokenSessionEntity refreshTokenSession = RefreshTokenSessionEntity.builder()
                .accountId(accountId)
                .refreshToken(refreshToken)
                .userAgent(userAgent)
                .ip(ip)
                .expiresAt(refreshTokenExpires)
                .build();

        refreshTokenSessionRepository.save(refreshTokenSession);


        return LoginConfirmResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpires(accessTokenExpires)
                .refreshTokenExpires(refreshTokenExpires)
                .build();
    }


    private LoginResponseDto validLoginSessionCreateAndSave(getAccountInfoProto.GetAccountInfoResponse response, String email) {
        String accountIdStr = response.getAccountId();
        UUID accountId = UUID.fromString(accountIdStr);

        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByAccountIdAndEmail(accountId, email);

        if (loginSessionOpt.isPresent()) {
            LoginSessionEntity session = loginSessionOpt.get();

            if (session.getCodeExpires().before(Timestamp.from(Instant.now()))) {
                String refreshCode = codeGeneratorService.codeGenerate();
                Timestamp refreshCodeExpires = codeGeneratorService.codeExpiresGenerate();
                session.setCode(refreshCode);
                session.setCodeExpires(refreshCodeExpires);
                loginSessionRepository.save(session);
                return new LoginResponseDto(refreshCodeExpires, refreshCode);
            }
            return new LoginResponseDto(session.getCodeExpires(), session.getCode());
        } else {
            String code = codeGeneratorService.codeGenerate();
            Timestamp codeExpires = codeGeneratorService.codeExpiresGenerate();
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

    private LoginResponseDto fakeLoginSessionCreateAndSave(String email) {

        Timestamp fakeCodeExpires = codeGeneratorService.codeExpiresGenerate();
        LoginSessionEntity loginSession = LoginSessionEntity.builder()
                .accountId(null)
                .email(email)
                .code("")
                .codeExpires(fakeCodeExpires)
                .build();
        loginSessionRepository.save(loginSession);
        return new LoginResponseDto(fakeCodeExpires, "");
    }

}