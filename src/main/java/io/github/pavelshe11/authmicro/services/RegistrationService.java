package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationConfirmRequestDto;
import io.github.pavelshe11.authmicro.api.dto.requests.RegistrationRequestDto;
import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.RegistrationSessionRepository;
import io.github.pavelshe11.authmicro.validators.RegistrationValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final PasswordEncoder passwordEncoder;
    private final RegistrationSessionRepository registrationSessionRepository;
    private final CodeGeneratorService registrationGeneratorService;
    private final AccountCreationRequestGrpcService accountCreationRequestGrpcService;
    private final RegistrationValidation registrationValidator;

    public RegistrationResponseDto register(RegistrationRequestDto registrationRequest) {

        String email = registrationRequest.getEmail();

        registrationValidator.validateRegistrationData(email,
                registrationRequest.getAcceptedPrivacyPolicy(),
                registrationRequest.getAcceptedPersonalDataProcessing());

        email = registrationValidator.getTrimmedEmail(email);

        registrationValidator.checkIfAccountExists(email);

        String code = registrationGeneratorService.codeGenerate();
        Instant codeExpires = registrationGeneratorService.codeExpiresGenerate();

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

        registrationValidator.validateRegistrationData(email,
                registrationConfirmRequest.getAcceptedPrivacyPolicy(),
                registrationConfirmRequest.getAcceptedPersonalDataProcessing());

        email = registrationValidator.getTrimmedEmail(email);

        RegistrationSessionEntity registrationSession = registrationSessionRepository
                .findByEmail(email)
                .orElse(null);

        if (registrationSession == null) {
            return ResponseEntity.ok().build();
        }

        registrationValidator.checkIfCodeIsValid(code, registrationSession, passwordEncoder);

        registrationValidator.ensureCodeIsNotExpired(registrationSession);

        boolean isAccountCreated = accountCreationRequestGrpcService.createAccount(email);

        if (!isAccountCreated) {
            throw new ServerAnswerException("Сервер не отвечает.");
        }

        return ResponseEntity.ok().build();
    }


    private RegistrationResponseDto refreshCodeAndReturnRegistrationResponseDto(RegistrationSessionEntity registrationSession, String code, Instant codeExpires) {
        registrationSession.setCode(registrationGeneratorService.CodeHash(code));
        registrationSession.setCodeExpires(codeExpires);
        registrationSessionRepository.save(registrationSession);

        return new RegistrationResponseDto(codeExpires, code);
    }

    private RegistrationResponseDto returnNewRegistrationResponseDto(String email, String code, Instant codeExpires) {
        RegistrationSessionEntity registrationSession =
                registrationSessionRepository.save(
                RegistrationSessionEntity.builder()
                        .email(email)
                        .acceptedPrivacyPolicy(true)
                        .acceptedPersonalDataProcessing(true)
                        .code(registrationGeneratorService.CodeHash(code))
                        .codeExpires(codeExpires)
                        .build()
        );

        return new RegistrationResponseDto(
                registrationSession.getCodeExpires(),
                code
        );
    }
}