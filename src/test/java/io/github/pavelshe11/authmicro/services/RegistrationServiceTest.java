package io.github.pavelshe11.authmicro.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pavelshe11.authmicro.api.client.grpc.AccountCreationRequestGrpc;
import io.github.pavelshe11.authmicro.api.client.grpc.AccountValidatorGrpc;
import io.github.pavelshe11.authmicro.api.client.grpc.GetAccountInfoGrpc;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.components.CodeGenerator;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import io.github.pavelshe11.authmicro.store.repositories.RefreshTokenSessionRepository;
import io.github.pavelshe11.authmicro.store.repositories.RegistrationSessionRepository;
import io.github.pavelshe11.authmicro.util.JwtUtil;
import io.github.pavelshe11.authmicro.validators.LoginValidation;
import io.github.pavelshe11.authmicro.validators.RegistrationValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    private RegistrationService registrationService;
    private RegistrationSessionRepository registrationSessionRepository;
    private CodeGenerator codeGenerator;
    private AccountCreationRequestGrpc accountCreationRequestGrpc;
    private RegistrationValidation registrationValidator;
    private AccountValidatorGrpc accountValidatorGrpc;
    private GetAccountInfoGrpc getAccountInfoGrpc;
    private SessionCleanerService sessionCleanerService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        registrationSessionRepository = mock(RegistrationSessionRepository.class);
        codeGenerator = mock(CodeGenerator.class);
        accountCreationRequestGrpc = mock(AccountCreationRequestGrpc.class);
        registrationValidator = mock(RegistrationValidation.class);
        accountValidatorGrpc = mock(AccountValidatorGrpc.class);
        getAccountInfoGrpc = mock(GetAccountInfoGrpc.class);
        sessionCleanerService = mock(SessionCleanerService.class);

        registrationService = new RegistrationService(
                registrationSessionRepository,
                codeGenerator,
                accountCreationRequestGrpc,
                registrationValidator,
                accountValidatorGrpc,
                getAccountInfoGrpc,
                sessionCleanerService
        );
    }

    @Test
    void testRegister_NewEmail_NoSession_CreatesNewRegistrationSession() throws Exception {
        // Arrange
        String email = "admin@communicator.ru";
        JsonNode requestJson = objectMapper.readTree("{\"email\": \"" + email + "\", \"name\": \"Test\"}");

        when(registrationValidator.getTrimmedEmailOrThrow(email)).thenReturn(email);
        doNothing().when(registrationValidator).validateEmailFormatOrThrow(email);

        AccountValidatorProto.ValidateUserDataResponse validationResponse =
                AccountValidatorProto.ValidateUserDataResponse.newBuilder().setAccept(true).build();
        when(accountValidatorGrpc.validateUserData(anyMap())).thenReturn(validationResponse);
        doNothing().when(registrationValidator).validateUserDataOrThrow(validationResponse);

        when(getAccountInfoGrpc.getAccountInfoByEmail(email)).thenReturn(Optional.empty());

        when(registrationSessionRepository.findByEmail(email)).thenReturn(Optional.empty());

        when(codeGenerator.codeGenerate()).thenReturn("123456");
        when(codeGenerator.codeExpiresGenerate()).thenReturn(System.currentTimeMillis() + 5 * 60 * 1000);
        when(codeGenerator.codeHash("123456")).thenReturn("hashed123456");
        when(codeGenerator.getCodePattern()).thenReturn("[0-9]{6}");

        // Act
        RegistrationResponseDto response = registrationService.register(requestJson);

        // Assert
        assertNotNull(response);
        assertTrue(response.getCodeExpires() > System.currentTimeMillis());
        assertEquals("[0-9]{6}", response.getCodePattern());

        verify(registrationSessionRepository, times(1)).save(any(RegistrationSessionEntity.class));
    }
}