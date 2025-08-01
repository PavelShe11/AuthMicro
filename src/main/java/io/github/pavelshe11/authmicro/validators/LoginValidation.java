package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import io.github.pavelshe11.authmicro.api.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.exceptions.FieldValidationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.*;

@RequiredArgsConstructor
@Component
public class LoginValidation {
    private final LoginSessionRepository loginSessionRepository;

    public String ValidateAndGetTrimmedEmail(String email) {
        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        if (email == null || email.trim().isEmpty()) {
            fieldErrors.add(
                    new FieldErrorDto("email", "поле пустое.")
            );
            throw new FieldValidationException(fieldErrors);
        }
        return email.trim();
    }

    public LoginSessionEntity getValidLoginSessionOrThrow(UUID accountId, String email) {
        Optional<LoginSessionEntity> loginSessionOpt = loginSessionRepository.findByAccountIdAndEmail(accountId, email);

        if (loginSessionOpt.isEmpty()) {
            throw new InvalidCodeException("error", "Неверный код подтверждения");
        }

        if (loginSessionOpt.get().getCodeExpires().before(new Timestamp(System.currentTimeMillis()))) {
            throw new CodeVerificationException("error", "Код подтверждения истёк. Пожалуйста, запросите новый код и попробуйте снова.");
        }
        return loginSessionOpt.get();
    }


    public void checkIfCodeIsValid(LoginSessionEntity session, String code) {
        if (!(Objects.equals(code, session.getCode()))) {
            throw new InvalidCodeException("error", "Неверный код подтверждения.");
        }
    }

    public void ensureCodeIsNotExpired(LoginSessionEntity session) {
        if (session.getCodeExpires().before(new Timestamp(System.currentTimeMillis()))) {
            throw new CodeVerificationException("error", "Код подтверждения истёк. Пожалуйста, запросите новый код.");
        }
    }
}
