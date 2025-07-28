package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import io.github.pavelshe11.authmicro.api.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.exceptions.FieldValidationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.services.EmailValidatorGrpcService;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RegistrationValidation {
    private final EmailValidatorGrpcService emailValidatorGrpcService;

    public void validateRegistrationData(String email,
                                         Boolean acceptedPrivacyPolicy,
                                         Boolean acceptedPersonalDataProcessing
    ) {
        List<FieldErrorDto> errors = new ArrayList<>();

        if (email == null || email.trim().isEmpty()) {
            errors.add(new FieldErrorDto("email", "Поле пустое"));
        }

        if (acceptedPrivacyPolicy == null || !acceptedPrivacyPolicy) {
            errors.add(new FieldErrorDto("acceptedPrivacyPolicy", "Не принято пользовательское соглашение."));
        }

        // data processing
        if (acceptedPersonalDataProcessing == null || !acceptedPersonalDataProcessing) {
            errors.add(new FieldErrorDto("acceptedPersonalDataProcessing", "Не принято соглашение на обработку персональных данных."));
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

    }

    public void checkIfAccountExists(String email) {
        try {
            if (emailValidatorGrpcService.isAccountExists(email)) {
                throw new ServerAnswerException("Сервер не отвечает.");
            }
        } catch (Exception e) {
            throw new ServerAnswerException("Сервер не отвечает.");
        }
    }

    public void ensureCodeIsExpired(RegistrationSessionEntity session) {
        if (session.getCodeExpires().isAfter(Instant.now())) {
            throw new CodeVerificationException("error", "Код еще не истёк.");
        }
    }

    public void ensureCodeIsNotExpired(RegistrationSessionEntity session) {
        if (session.getCodeExpires().isBefore(Instant.now())) {
            throw new CodeVerificationException("error", "Код подтверждения истёк. Пожалуйста, запросите новый код.");
        }
    }

    public void checkIfCodeIsValid(String code, RegistrationSessionEntity registrationSession, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(code, registrationSession.getCode())) {
            throw new InvalidCodeException("error", "Неверный код подтверждения.");
        }
    }

    public String getTrimmedEmail(String email) {
        return email.trim();
    }
}
