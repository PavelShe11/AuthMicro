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
            throw new BadRequestException("Поле Email не может быть пустым.");
        }
        else return email.trim();
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

    public void checkIfCodeInExistingSessionExpired(RegistrationSessionEntity registrationSession) {
        if (registrationSession.getCodeExpires().isAfter(Instant.now())) {
            throw new CodeVerificationException("Код не истёк.");
        } else if (registrationSession.getCodeExpires().isBefore(Instant.now())) {
            throw new CodeVerificationException("Код подтверждения истёк. Пожалуйста, запросите новый код и попробуйте снова.");
        }
    }

    public void checkIfCodeIsValid(String code, RegistrationSessionEntity registrationSession, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(code, registrationSession.getCode())) {
            throw new InvalidCodeException("Неверный код подтверждения.");
        }
    }

}
