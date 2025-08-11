package io.github.pavelshe11.authmicro.services;


import io.github.pavelshe11.authmicro.api.client.grpc.GetAccountInfoGrpc;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginConfirmResponseDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.components.CodeGenerator;
import io.github.pavelshe11.authmicro.grpc.getAccountInfoProto;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.entities.RefreshTokenSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import io.github.pavelshe11.authmicro.store.repositories.RefreshTokenSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import io.github.pavelshe11.authmicro.validators.LoginValidation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;


@Service
@RequiredArgsConstructor
public class LoginService {
    private final LoginSessionRepository loginSessionRepository;
    private final CodeGenerator codeGenerator;
    private final JwtUtil jwtUtil;
    private final GetAccountInfoGrpc getAccountInfoGrpc;
    private final LoginValidation loginValidator;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    public LoginResponseDto login(String email) {

        email = loginValidator.getTrimmedEmailOrThrow(email);
        loginValidator.validateEmailFormatOrThrow(email);

        Optional<getAccountInfoProto.GetAccountInfoResponse> accountInfoOpt =
                getAccountInfoGrpc.getAccountInfoByEmail(email);

        if (accountInfoOpt.isEmpty() || !accountInfoOpt.get().getUserDataMap().containsKey("account_id")) {
            return fakeLoginSessionCreateAndSave(email);
        }
        getAccountInfoProto.GetAccountInfoResponse response = accountInfoOpt.get();

        return validLoginSessionCreateAndSave(response, email);

    }

    @Transactional
    public LoginConfirmResponseDto confirmLoginEmail(String email, String code, String ip, String userAgent) {
        email = loginValidator.getTrimmedEmailOrThrow(email);
        loginValidator.validateEmailFormatOrThrow(email);
        code = loginValidator.getTrimmedCodeOrThrow(code);

        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByEmail(email);

        LoginSessionEntity loginSession = loginValidator.validateLoginSessionOrThrow(loginSessionOpt);

        loginValidator.checkIfCodeIsValid(loginSession, code);
        loginValidator.ensureCodeIsNotExpired(loginSession);

        Optional<getAccountInfoProto.GetAccountInfoResponse> accountInfoOpt =
                getAccountInfoGrpc.getAccountInfoByEmail(email);

        if (accountInfoOpt.isEmpty()) {
            loginSessionRepository.delete(loginSession);
            throw new InvalidCodeException();
        }

        getAccountInfoProto.GetAccountInfoResponse accountInfo = accountInfoOpt.get();

        Map<String, com.google.protobuf.Value> userData = accountInfo.getUserDataMap();

        String accountIdStr = Optional.ofNullable(userData.get("account_id"))
                .map(com.google.protobuf.Value::getStringValue)
                .orElseThrow(() -> new InvalidCodeException());

        String role = Optional.ofNullable(userData.get("role"))
                .map(com.google.protobuf.Value::getStringValue)
                .orElse("user");

        UUID accountId = UUID.fromString(accountIdStr);

        if (!accountId.equals(loginSession.getAccountId())) {
            throw new InvalidCodeException();
        }

        boolean isAdmin = "admin".equals(role);

        Map<String, Object> tokens = generateTokens(accountId, isAdmin);


        refreshTokenSessionCreateAndSave(ip, userAgent, accountId,
                (String) tokens.get("refreshToken"),
                new Timestamp((Long) tokens.get("refreshTokenExpires")));

        loginSessionRepository.delete(loginSession);

        return LoginConfirmResponseBuild(tokens);
    }


    private static LoginConfirmResponseDto LoginConfirmResponseBuild(Map<String, Object> tokens) {
        return LoginConfirmResponseDto.builder()
                .accessToken((String) tokens.get("accessToken"))
                .refreshToken((String) tokens.get("refreshToken"))
                .accessTokenExpires((long) tokens.get("accessTokenExpires"))
                .refreshTokenExpires((long) tokens.get("refreshTokenExpires"))
                .build();
    }

    private Map<String, Object> generateTokens(UUID accountId, boolean isAdmin) {
        String accessToken = jwtUtil.generateAccessToken(accountId, isAdmin);
        String refreshToken = jwtUtil.generateRefreshToken(accountId);
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("accessTokenExpires", jwtUtil.extractExpiration(accessToken).getTime());
        tokens.put("refreshTokenExpires", jwtUtil.extractExpiration(refreshToken).getTime());
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

        Map<String, com.google.protobuf.Value> userData = response.getUserDataMap();

        String accountIdStr = Optional.ofNullable(userData.get("account_id"))
                .map(com.google.protobuf.Value::getStringValue)
                .orElseThrow(() -> new ServerAnswerException());
        UUID accountId = UUID.fromString(accountIdStr);

        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByEmail(email);

        if (loginSessionOpt.isPresent()) {
            LoginSessionEntity session = loginSessionOpt.get();

            if (session.getAccountId() == null || !session.getAccountId().equals(accountId)
                    || session.getCodeExpires().before(Timestamp.from(Instant.now()))) {
                String rawRefreshCode = codeGenerator.codeGenerate();
                String hashedRefreshCode = codeGenerator.codeHash(rawRefreshCode);
                long refreshCodeExpires = codeGenerator.codeExpiresGenerate();
                session.setAccountId(accountId);
                session.setCode(hashedRefreshCode);
                session.setCodeExpires(new Timestamp(refreshCodeExpires));
                log.info("LOGIN_CODE email={} code={}", email, rawRefreshCode);
                loginSessionRepository.save(session);
                return new LoginResponseDto(refreshCodeExpires);
            }
            return new LoginResponseDto(session.getCodeExpires().getTime());
        }

        String rawCode = codeGenerator.codeGenerate();
        String hashedCode = codeGenerator.codeHash(rawCode);
        long codeExpires = codeGenerator.codeExpiresGenerate();
        LoginSessionEntity loginSession = LoginSessionEntity.builder()
                .accountId(accountId)
                .email(email)
                .code(hashedCode)
                .codeExpires(new Timestamp(codeExpires))
                .build();
        log.info("LOGIN_CODE email={} code={}", email, rawCode);
        loginSessionRepository.save(loginSession);
        return new LoginResponseDto(codeExpires);

    }

    private LoginResponseDto fakeLoginSessionCreateAndSave(String email) {

        long fakeCodeExpires = codeGenerator.codeExpiresGenerate();
        LoginSessionEntity loginSession = LoginSessionEntity.builder()
                .accountId(null)
                .email(email)
                .code("")
                .codeExpires(new Timestamp(fakeCodeExpires))
                .build();
        log.info("FAKE_LOGIN_CODE email={} code={}", email, "");
        loginSessionRepository.findByEmail(email)
                .ifPresent((session) -> loginSessionRepository.delete(session));
        loginSessionRepository.save(loginSession);
        return new LoginResponseDto(fakeCodeExpires);
    }

}