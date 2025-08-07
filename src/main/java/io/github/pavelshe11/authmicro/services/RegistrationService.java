package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.api.client.grpc.AccountCreationRequestGrpc;
import io.github.pavelshe11.authmicro.api.client.grpc.AccountValidatorGrpc;
import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationConfirmRequestDto;
import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationRequestDto;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.RegistrationSessionRepository;
import io.github.pavelshe11.authmicro.validators.RegistrationValidation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final RegistrationSessionRepository registrationSessionRepository;
    private final CodeGeneratorService registrationGeneratorService;
    private final AccountCreationRequestGrpc accountCreationRequestGrpc;
    private final RegistrationValidation registrationValidator;
    private final AccountValidatorGrpc accountValidatorGrpc;

    public RegistrationResponseDto register(RegistrationRequestDto registrationRequest) {

        String email = registrationRequest.getEmail();
        email = registrationValidator.getTrimmedEmailOrThrow(email);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse
                = accountValidatorGrpc.validateUserData(
                Map.of(
                        "email", registrationRequest.getEmail(),
                        "acceptedPrivacyPolicy", registrationRequest.getAcceptedPrivacyPolicy(),
                        "acceptedPersonalDataProcessing", registrationRequest.getAcceptedPersonalDataProcessing()
                )
        );

        registrationValidator.validateUserDataOrThrow(accountValidatorResponse);

        if (!accountValidatorResponse.getAccept()) {
            return fakeRegistrationSessionCreate(email);
        }

        String code = registrationGeneratorService.codeGenerate();
        Timestamp codeExpires = registrationGeneratorService.codeExpiresGenerate();

        RegistrationResponseDto existingRegistrationSession = handleExistingRegistrationSession(email, code, codeExpires);
        if (existingRegistrationSession != null) return existingRegistrationSession;

        return returnNewRegistrationResponseDto(email, code, codeExpires);
    }

    public ResponseEntity<Void> confirmEmail(RegistrationConfirmRequestDto registrationConfirmRequest, HttpServletRequest httpRequest) {

        String email = registrationConfirmRequest.getEmail();
        String code = registrationConfirmRequest.getCode();

        email = registrationValidator.getTrimmedEmailOrThrow(email);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse
                = accountValidatorGrpc.validateUserData(
                Map.of(
                        "email", registrationConfirmRequest.getEmail(),
                        "acceptedPrivacyPolicy", registrationConfirmRequest.getAcceptedPrivacyPolicy(),
                        "acceptedPersonalDataProcessing", registrationConfirmRequest.getAcceptedPersonalDataProcessing()
                )
        );

        registrationValidator.validateUserDataOrThrow(accountValidatorResponse);

        RegistrationSessionEntity registrationSession = registrationSessionRepository
                .findByEmail(email)
                .orElse(null);

        if (registrationSession == null) {
            return ResponseEntity.ok().build();
        }

        registrationValidator.checkIfCodeIsValid(code, registrationSession);

        registrationValidator.ensureCodeIsNotExpired(registrationSession);

        boolean isAccountCreated = accountCreationRequestGrpc.createAccount(
                Map.of(
                        "email", registrationConfirmRequest.getEmail(),
                        "acceptedPrivacyPolicy", registrationConfirmRequest.getAcceptedPrivacyPolicy(),
                        "acceptedPersonalDataProcessing", registrationConfirmRequest.getAcceptedPersonalDataProcessing(),
                        "ip", httpRequest.getRemoteAddr()
                )
        );

        if (!isAccountCreated) {
            throw new ServerAnswerException();
        }

        return ResponseEntity.ok().build();
    }


    private RegistrationResponseDto fakeRegistrationSessionCreate(String email) {
        Timestamp fakeCodeExpires = registrationGeneratorService.codeExpiresGenerate();

        RegistrationSessionEntity fakeSession = RegistrationSessionEntity.builder()
                .email(email)
                .acceptedPrivacyPolicy(false)
                .acceptedPersonalDataProcessing(false)
                .code("")
                .codeExpires(fakeCodeExpires)
                .build();

        registrationSessionRepository.save(fakeSession);

        return new RegistrationResponseDto(fakeCodeExpires, "");
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
        registrationSession.setCode(code);
        registrationSession.setCodeExpires(codeExpires);
        registrationSessionRepository.save(registrationSession);

        return new RegistrationResponseDto(codeExpires, code);
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
                registrationSession.getCodeExpires(),
                code
        );
    }
}