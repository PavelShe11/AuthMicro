package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.api.client.grpc.GetAccountInfoGrpc;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.components.CodeGenerator;
import io.github.pavelshe11.authmicro.grpc.getAccountInfoProto;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import io.github.pavelshe11.authmicro.store.repositories.RefreshTokenSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import io.github.pavelshe11.authmicro.validators.LoginValidation;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {


    @org.junit.jupiter.api.Test
    void login() {

        LoginSessionRepository loginSessionRepository = mock(LoginSessionRepository.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        GetAccountInfoGrpc getAccountInfoGrpc = mock(GetAccountInfoGrpc.class);
        LoginValidation loginValidator = mock(LoginValidation.class);
        RefreshTokenSessionRepository refreshTokenSessionRepository = mock(RefreshTokenSessionRepository.class);

        LoginService loginService = new LoginService(
                loginSessionRepository,
                codeGenerator,
                jwtUtil,
                getAccountInfoGrpc,
                loginValidator,
                refreshTokenSessionRepository
        );

        // Arrange (подготовка)
        String email = "admin@communicator.ru";
        String trimmedEmail = email.trim();
        String rawCode = "123456";
        String hashedCode = "hashedCode";
        long codeExpiresConst = Instant.now().plusSeconds(100).toEpochMilli();
        Timestamp codeExpires = new Timestamp(codeExpiresConst);
        UUID accountId = UUID.randomUUID();

        getAccountInfoProto.GetAccountInfoResponse accountInfo = getAccountInfoProto.GetAccountInfoResponse
                .newBuilder()
                .setAccountId(accountId.toString())
                .setRole("admin")
                .build();

        when(loginValidator.getTrimmedEmailOrThrow(email)).thenReturn(trimmedEmail);
        when(getAccountInfoGrpc.getAccountInfo(trimmedEmail)).thenReturn(Optional.of(accountInfo));
        when(loginSessionRepository.findByEmail(trimmedEmail)).thenReturn(Optional.empty());
        when(codeGenerator.codeGenerate()).thenReturn(rawCode);
        when(codeGenerator.codeHash(rawCode)).thenReturn(hashedCode);
        when(codeGenerator.codeExpiresGenerate()).thenReturn(codeExpiresConst);

        // Act (действие)
        LoginResponseDto response = loginService.login(email);

        // Assert (проверка)
        ArgumentCaptor<LoginSessionEntity> captor = ArgumentCaptor.forClass(LoginSessionEntity.class);
        verify(loginSessionRepository).save(captor.capture());

        LoginSessionEntity savedSession = captor.getValue();
        assertEquals(trimmedEmail, savedSession.getEmail());
        assertEquals(hashedCode, savedSession.getCode());
        assertEquals(accountId, savedSession.getAccountId());
        assertEquals(codeExpires, savedSession.getCodeExpires());

        assertEquals(codeExpiresConst, response.getCodeExpires());

    }

}