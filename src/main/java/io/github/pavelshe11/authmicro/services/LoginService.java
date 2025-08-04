package io.github.pavelshe11.authmicro.services;


import io.github.pavelshe11.authmicro.api.client.grpc.GetAccountInfoGrpc;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
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

        email = loginValidator.getTrimmedEmailOrThrow(email);

        Optional<getAccountInfoProto.GetAccountInfoResponse> accountInfoOpt =
                getAccountInfoGrpc.getAccountInfo(email);

        if (accountInfoOpt.isEmpty()) {
            return fakeLoginSessionCreateAndSave(email);
        }
        getAccountInfoProto.GetAccountInfoResponse response = accountInfoOpt.get();

        return validLoginSessionCreateAndSave(response, email);

    }

    public LoginConfirmResponseDto confirmLoginEmail(String email, String code, String ip, String userAgent) {
        email = loginValidator.getTrimmedEmailOrThrow(email);
        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByEmail(email);

        LoginSessionEntity loginSession = loginValidator.validateLoginSessionOrThrow(loginSessionOpt);

        loginValidator.checkIfCodeIsValid(loginSession, code);
        loginValidator.ensureCodeIsNotExpired(loginSession);

        getAccountInfoProto.GetAccountInfoResponse accountInfo = getAccountInfoGrpc.getAccountInfo(email)
                .orElseThrow(() -> new ServerAnswerException("Сервер не отвечает."));


        UUID accountId = UUID.fromString(accountInfo.getAccountId());
        if (!accountId.equals(loginSession.getAccountId())) {
            throw new ServerAnswerException("Сервер не отвечает.");
        }

        boolean isAdmin = "admin" .equals(accountInfo.getRole());

        Map<String, Object> tokens = generateTokens(accountId, isAdmin);

        loginSessionRepository.save(loginSession);

        refreshTokenSessionCreateAndSave(ip, userAgent, accountId,
                (String) tokens.get("refreshToken"), (Timestamp) tokens.get("refreshTokenExpires"));


        return LoginConfirmResponseBuild(tokens);
    }


    private static LoginConfirmResponseDto LoginConfirmResponseBuild(Map<String, Object> tokens) {
        return LoginConfirmResponseDto.builder()
                .accessToken((String) tokens.get("accessToken"))
                .refreshToken((String) tokens.get("refreshToken"))
                .accessTokenExpires((Timestamp) tokens.get("accessTokenExpires"))
                .refreshTokenExpires((Timestamp) tokens.get("refreshTokenExpires"))
                .build();
    }

    private Map<String, Object> generateTokens(UUID accountId, boolean isAdmin) {
        String accessToken = jwtUtil.generateAccessToken(accountId, isAdmin);
        String refreshToken = jwtUtil.generateRefreshToken(accountId, isAdmin);
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("accessTokenExpires", jwtUtil.extractExpiration(accessToken));
        tokens.put("refreshTokenExpires", jwtUtil.extractExpiration(refreshToken));
        return tokens;
    }

    private void refreshTokenSessionCreateAndSave(String ip, String userAgent, UUID accountId, String refreshToken, Timestamp refreshTokenExpires) {
        RefreshTokenSessionEntity refreshTokenSession = RefreshTokenSessionEntity.builder()
                .accountId(accountId)
                .refreshToken(refreshToken)
                .userAgent(userAgent)
                .ip(ip)
                .expiresAt(refreshTokenExpires)
                .build();

        refreshTokenSessionRepository.save(refreshTokenSession);
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