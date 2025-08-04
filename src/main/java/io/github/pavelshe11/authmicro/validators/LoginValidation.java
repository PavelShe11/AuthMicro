package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import io.github.pavelshe11.authmicro.api.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.exceptions.FieldValidationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.*;

@RequiredArgsConstructor
@Component
public class LoginValidation {

    public String getTrimmedEmailOrThrow(String email) {
        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        if (email == null || email.trim().isEmpty()) {
            fieldErrors.add(
                    new FieldErrorDto("email", "поле пустое.")
            );
            throw new FieldValidationException("Ошибка входа", fieldErrors);
        }
        return email.trim();
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

    public LoginSessionEntity validateLoginSessionOrThrow(Optional<LoginSessionEntity> loginSessionOpt) {
        if (loginSessionOpt.isEmpty()) {
            throw new ServerAnswerException("Сервер не отвечает.");
        }
        LoginSessionEntity loginSession = loginSessionOpt.get();

        if (loginSession.getAccountId() == null) {
            throw new InvalidCodeException("error", "Неверный код подтверждения.");
        }
        return loginSession;
    }
}
