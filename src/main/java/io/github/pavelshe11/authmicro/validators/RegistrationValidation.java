package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.exceptions.BadRequestException;
import io.github.pavelshe11.authmicro.api.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.services.EmailValidatorGrpcService;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RegistrationValidation {
    private final EmailValidatorGrpcService emailValidatorGrpcService;

    public String validateAndTrimEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("email", "Поле пустое.");
        } else return email.trim();
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

    public void checkIfPolicyAgreementsAcceptedOfThrow(
            Boolean acceptedPrivacyPolicy,
            Boolean acceptedPersonalDataProcessing) {

        if (acceptedPrivacyPolicy == null || !acceptedPrivacyPolicy) {
            throw new BadRequestException("acceptedPrivacyPolicy", "Не принято пользовательское соглашение.");
        }
        if (acceptedPersonalDataProcessing == null || !acceptedPersonalDataProcessing) {
            throw new BadRequestException("acceptedPrivacyPolicy", "Не принято соглашение на обработку персональных данных.");
        }
    }
}
