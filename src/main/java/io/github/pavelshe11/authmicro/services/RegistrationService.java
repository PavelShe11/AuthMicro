package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.api.grpc.client.AccountCreationRequestGrpc;
import io.github.pavelshe11.authmicro.api.grpc.client.AccountValidatorGrpc;
import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationConfirmRequestDto;
import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationRequestDto;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.RegistrationSessionRepository;
import io.github.pavelshe11.authmicro.validators.RegistrationValidation;
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
        email = registrationValidator.getTrimmedEmail(email);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse
                = accountValidatorGrpc.validateUserData(
                Map.of(
                        "email", registrationRequest.getEmail(),
                        "acceptedPrivacyPolicy", registrationRequest.getAcceptedPrivacyPolicy(),
                        "acceptedPersonalDataProcessing", registrationRequest.getAcceptedPersonalDataProcessing(),
                        "typeOfActivity", "registration"
                )
        );

        registrationValidator.validateUserDataOrThrow(accountValidatorResponse);

        String code = registrationGeneratorService.codeGenerate();
        Timestamp codeExpires = registrationGeneratorService.codeExpiresGenerate();

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

        return returnNewRegistrationResponseDto(email, code, codeExpires);
    }


    public ResponseEntity<Void> confirmEmail(RegistrationConfirmRequestDto registrationConfirmRequest) {

        String email = registrationConfirmRequest.getEmail();
        String code = registrationConfirmRequest.getCode();

        email = registrationValidator.getTrimmedEmail(email);

        AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse
                = accountValidatorGrpc.validateUserData(
                Map.of(
                        "email", registrationConfirmRequest.getEmail(),
                        "acceptedPrivacyPolicy", registrationConfirmRequest.getAcceptedPrivacyPolicy(),
                        "acceptedPersonalDataProcessing", registrationConfirmRequest.getAcceptedPersonalDataProcessing(),
                        "typeOfActivity", "registration"
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

        boolean isAccountCreated = accountCreationRequestGrpc.createAccount(email);

        if (!isAccountCreated) {
            throw new ServerAnswerException("Сервер не отвечает.");
        }

        return ResponseEntity.ok().build();
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