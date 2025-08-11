package io.github.pavelshe11.authmicro.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pavelshe11.authmicro.api.client.grpc.AccountCreationRequestGrpc;
import io.github.pavelshe11.authmicro.api.client.grpc.AccountValidatorGrpc;
import io.github.pavelshe11.authmicro.api.client.grpc.GetAccountInfoGrpc;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.components.CodeGenerator;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.grpc.getAccountInfoProto;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.RegistrationSessionRepository;
import io.github.pavelshe11.authmicro.validators.RegistrationValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final RegistrationSessionRepository registrationSessionRepository;
    private final CodeGenerator codeGenerator;
    private final AccountCreationRequestGrpc accountCreationRequestGrpc;
    private final RegistrationValidation registrationValidator;
    private final AccountValidatorGrpc accountValidatorGrpc;
    private final GetAccountInfoGrpc getAccountInfoGrpc;

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    public RegistrationResponseDto register(JsonNode registrationRequest) {

        String email = registrationRequest.path("email").asText(null);
        email = registrationValidator.getTrimmedEmailOrThrow(email);
        registrationValidator.validateEmailFormatOrThrow(email);

        Map<String, Object> userData = convertJsonNodeToMap(registrationRequest);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse
                = accountValidatorGrpc.validateUserData(userData);

        registrationValidator.validateUserDataOrThrow(accountValidatorResponse);

        Optional<getAccountInfoProto.GetAccountInfoResponse> accountInfoOpt =
                getAccountInfoGrpc.getAccountInfoByEmail(email);


        if (accountInfoOpt.isPresent() && accountExists(accountInfoOpt.get())) {
            return fakeRegistrationSessionCreateAndSave(email);
        }

        registrationSessionRepository.findByEmail(email)
                .ifPresent(registrationSessionRepository::delete);

        RegistrationResponseDto existingRegistrationSession = handleExistingRegistrationSession(email);
        if (existingRegistrationSession != null) return existingRegistrationSession;

        String rawCode = codeGenerator.codeGenerate();
        String hashedCode = codeGenerator.codeHash(rawCode);
        long codeExpires = codeGenerator.codeExpiresGenerate();
        log.info("REGISTRATION_CODE email={} code={}", email, rawCode);

        return returnNewRegistrationResponseDto(email, hashedCode, new Timestamp(codeExpires));
    }

    @Transactional
    public void confirmEmail(JsonNode registrationConfirmRequest, String ip) {

        String email = registrationConfirmRequest.path("email").asText(null);
        String code = registrationConfirmRequest.path("code").asText(null);

        email = registrationValidator.getTrimmedEmailOrThrow(email);
        registrationValidator.validateEmailFormatOrThrow(email);
        code = registrationValidator.getTrimmedCodeOrThrow(code);

        Map<String, Object> userData = convertJsonNodeToMap(registrationConfirmRequest);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse
                = accountValidatorGrpc.validateUserData(userData);

        registrationValidator.validateUserDataOrThrow(accountValidatorResponse);

        RegistrationSessionEntity registrationSession = registrationSessionRepository
                .findByEmail(email)
                .orElse(null);

        if (registrationSession == null) {
            throw new InvalidCodeException();
        }

        registrationValidator.checkIfCodeIsValid(code, registrationSession);

        registrationValidator.ensureCodeIsNotExpired(registrationSession);

        Optional<getAccountInfoProto.GetAccountInfoResponse> accountInfoOpt =
                getAccountInfoGrpc.getAccountInfoByEmail(email);

        if (accountInfoOpt.isPresent() && accountExists(accountInfoOpt.get())) {
            throw new InvalidCodeException();
        }

        userData.put("ip", ip);

        boolean isAccountCreated = accountCreationRequestGrpc.createAccount(userData);


        if (!isAccountCreated) {
            throw new ServerAnswerException();
        }

        registrationSessionRepository.delete(registrationSession);
    }


    private Map<String, Object> convertJsonNodeToMap(JsonNode registrationRequest) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(registrationRequest, new TypeReference<>() {
        });
    }

    private RegistrationResponseDto fakeRegistrationSessionCreateAndSave(String email) {
        long fakeCodeExpires = codeGenerator.codeExpiresGenerate();

        RegistrationSessionEntity fakeSession = registrationSessionRepository
                .findByEmail(email)
                .orElseGet(() -> new RegistrationSessionEntity());

        fakeSession.setEmail(email);
        fakeSession.setAcceptedPrivacyPolicy(false);
        fakeSession.setAcceptedPersonalDataProcessing(false);
        fakeSession.setCode("");
        fakeSession.setCodeExpires(new Timestamp(fakeCodeExpires));

        log.info("FAKE_REGISTRATION_CODE email={} code={}", email, "");

        registrationSessionRepository.save(fakeSession);

        return new RegistrationResponseDto(fakeCodeExpires, codeGenerator.getCodePattern());
    }


    private RegistrationResponseDto handleExistingRegistrationSession(String email) {
        RegistrationSessionEntity registrationSession = registrationSessionRepository
                .findByEmail(email)
                .orElse(null);

        if (registrationSession != null) {
            if (!registrationValidator.isCodeExpired(registrationSession)) {
                return new RegistrationResponseDto(registrationSession.getCodeExpires().getTime(),
                        codeGenerator.getCodePattern());
            }
            String rawCode = codeGenerator.codeGenerate();
            long codeExpires = codeGenerator.codeExpiresGenerate();

            return refreshCodeAndReturnRegistrationResponseDto(
                    registrationSession,
                    rawCode, new Timestamp(codeExpires)
            );
        }
        return null;
    }

    private RegistrationResponseDto refreshCodeAndReturnRegistrationResponseDto(RegistrationSessionEntity registrationSession, String code, Timestamp codeExpires) {
        String hashedCode = codeGenerator.codeHash(code);
        registrationSession.setCode(hashedCode);
        registrationSession.setCodeExpires(codeExpires);
        registrationSessionRepository.save(registrationSession);
        log.info("REGISTRATION_CODE email={} code={}", registrationSession.getEmail(), code);

        return new RegistrationResponseDto(codeExpires.getTime(),
                codeGenerator.getCodePattern());
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
                registrationSession.getCodeExpires().getTime(),
                codeGenerator.getCodePattern()
        );
    }

    private boolean accountExists(getAccountInfoProto.GetAccountInfoResponse response) {
        return response.getUserDataMap().containsKey("account_id");
    }
}