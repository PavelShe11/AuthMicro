package io.github.pavelshe11.authmicro.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pavelshe11.authmicro.api.client.grpc.AccountCreationRequestGrpc;
import io.github.pavelshe11.authmicro.api.client.grpc.AccountValidatorGrpc;
import io.github.pavelshe11.authmicro.api.client.grpc.GetAccountInfoGrpc;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.components.CodeGenerator;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.grpc.getAccountInfoProto;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.RegistrationSessionRepository;
import io.github.pavelshe11.authmicro.validators.RegistrationValidation;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final RegistrationSessionRepository registrationSessionRepository;
    private final CodeGenerator registrationGeneratorService;
    private final AccountCreationRequestGrpc accountCreationRequestGrpc;
    private final RegistrationValidation registrationValidator;
    private final AccountValidatorGrpc accountValidatorGrpc;
    private final GetAccountInfoGrpc getAccountInfoGrpc;

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    public RegistrationResponseDto register(JsonNode registrationRequest) {

        String email = registrationRequest.path("email").asText(null);
        email = registrationValidator.getTrimmedEmailOrThrow(email);

        Map<String, Object> userData = convertJsonNodeToMap(registrationRequest);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse
                = accountValidatorGrpc.validateUserData(userData);

        registrationValidator.validateUserDataOrThrow(accountValidatorResponse);

        Optional<getAccountInfoProto.GetAccountInfoResponse> accountInfoOpt =
                getAccountInfoGrpc.getAccountInfo(email);

        if (accountInfoOpt.isPresent()) {
            return fakeRegistrationSessionCreateAndSave(email);
        }

        String rawCode = registrationGeneratorService.codeGenerate();
        String hashedCode = registrationGeneratorService.codeHash(rawCode);
        long codeExpires = registrationGeneratorService.codeExpiresGenerate();
        log.info("REGISTRATION_CODE email={} code={}", email, rawCode);

        RegistrationResponseDto existingRegistrationSession = handleExistingRegistrationSession(email, hashedCode, new Timestamp(codeExpires));
        if (existingRegistrationSession != null) return existingRegistrationSession;

        return returnNewRegistrationResponseDto(email, hashedCode, new Timestamp(codeExpires));
    }


    public ResponseEntity<Void> confirmEmail(JsonNode registrationConfirmRequest, HttpServletRequest httpRequest) {

        String email = registrationConfirmRequest.path("email").asText(null);
        String code = registrationConfirmRequest.path("code").asText(null);

        email = registrationValidator.getTrimmedEmailOrThrow(email);

        Map<String, Object> userData = convertJsonNodeToMap(registrationConfirmRequest);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse
                = accountValidatorGrpc.validateUserData(userData);

        registrationValidator.validateUserDataOrThrow(accountValidatorResponse);

        RegistrationSessionEntity registrationSession = registrationSessionRepository
                .findByEmail(email)
                .orElse(null);

        if (registrationSession == null) {
            return ResponseEntity.ok().build();
        }

        registrationValidator.checkIfCodeIsValid(code, registrationSession);

        registrationValidator.ensureCodeIsNotExpired(registrationSession);

        userData.put("ip", httpRequest.getRemoteAddr());

        boolean isAccountCreated = accountCreationRequestGrpc.createAccount(userData);


        if (!isAccountCreated) {
            throw new ServerAnswerException();
        }

        return ResponseEntity.ok().build();
    }



    private Map<String, Object> convertJsonNodeToMap(JsonNode registrationRequest) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(registrationRequest, new TypeReference<>() {});
    }

    private RegistrationResponseDto fakeRegistrationSessionCreateAndSave(String email) {
        long fakeCodeExpires = registrationGeneratorService.codeExpiresGenerate();

        RegistrationSessionEntity fakeSession = RegistrationSessionEntity.builder()
                .email(email)
                .acceptedPrivacyPolicy(false)
                .acceptedPersonalDataProcessing(false)
                .code("")
                .codeExpires(new Timestamp(fakeCodeExpires))
                .build();
        log.info("FAKE_REGISTRATION_CODE email={} code={}", email, "");

        registrationSessionRepository.save(fakeSession);

        return new RegistrationResponseDto(fakeCodeExpires);
    }


    private RegistrationResponseDto handleExistingRegistrationSession(String email, String code, Timestamp codeExpires) {
        RegistrationSessionEntity registrationSession = registrationSessionRepository
                .findByEmail(email)
                .orElse(null);

        if (registrationSession != null) {
            registrationValidator.ensureCodeIsExpired(registrationSession);

            return refreshCodeAndReturnRegistrationResponseDto(
                    registrationSession,
                    code, codeExpires
            );
        }
        return null;
    }

    private RegistrationResponseDto refreshCodeAndReturnRegistrationResponseDto(RegistrationSessionEntity registrationSession, String code, Timestamp codeExpires) {
        String hashedCode = registrationGeneratorService.codeHash(code);
        registrationSession.setCode(hashedCode);
        registrationSession.setCodeExpires(codeExpires);
        registrationSessionRepository.save(registrationSession);
        log.info("REGISTRATION_CODE email={} code={}", registrationSession.getEmail(), code);

        return new RegistrationResponseDto(codeExpires.getTime());
    }

    private RegistrationResponseDto returnNewRegistrationResponseDto(String email, String code, Timestamp codeExpires) {
        RegistrationSessionEntity registrationSession =
                registrationSessionRepository.save(
                        RegistrationSessionEntity.builder()
                                .email(email)
                                .acceptedPrivacyPolicy(true)
                                .acceptedPersonalDataProcessing(true)
                                .code(code)
                                .codeExpires(codeExpires)
                                .build()
                );

        return new RegistrationResponseDto(
                registrationSession.getCodeExpires().getTime()
        );
    }
}