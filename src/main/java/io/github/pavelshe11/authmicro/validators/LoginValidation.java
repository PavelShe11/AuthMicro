package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import io.github.pavelshe11.authmicro.api.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.exceptions.FieldValidationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.services.EmailValidatorGrpcService;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoginValidation {
    private final EmailValidatorGrpcService emailValidatorGrpcService;
    private final LoginSessionRepository loginSessionRepository;
    public String validateAndTrimEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            List<FieldErrorDto> errors = List.of(new FieldErrorDto("email",
                    "Поле пустое"));
            throw new FieldValidationException(errors);
        }
        else return email.trim();
    }

    public UUID getAccountIdByEmailOrThrow(String email) {
        return emailValidatorGrpcService.getAccountIdIfExists(email)
                .map(UUID::fromString)
                .orElseThrow(() -> new ServerAnswerException("Сервер не отвечает."));

    }

    public LoginSessionEntity getValidLoginSessionOrThrow(UUID accountId, String email) {
        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByAccountIdAndEmail(accountId, email);

        if (loginSessionOpt.isEmpty()) {
            throw new InvalidCodeException("error", "Неверный код подтверждения");
        }

        if (loginSessionOpt.get().getCodeExpires().isBefore(Instant.now())) {
            throw new CodeVerificationException("error", "Код подтверждения истёк. Пожалуйста, запросите новый код и попробуйте снова.");
        }
        return loginSessionOpt.get();
    }


    public void checkIfCodeIsValid(LoginSessionEntity session, String code, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(code, session.getCode())) {
            throw new InvalidCodeException("error", "Неверный код подтверждения.");
        }
    }

    public void ensureCodeIsExpired(LoginSessionEntity session) {
        if (session.getCodeExpires().isAfter(Instant.now())) {
            throw new CodeVerificationException("error", "Код еще не истёк.");
        }
    }

    public void ensureCodeIsNotExpired(LoginSessionEntity session) {
        if (session.getCodeExpires().isBefore(Instant.now())) {
            throw new CodeVerificationException("error", "Код подтверждения истёк. Пожалуйста, запросите новый код.");
        }
    }

}
