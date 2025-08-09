package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import io.github.pavelshe11.authmicro.api.exceptions.CodeExpiredException;
import io.github.pavelshe11.authmicro.api.exceptions.FieldValidationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.*;

@RequiredArgsConstructor
@Component
public class LoginValidation {

    private final PasswordEncoder passwordEncoder;

    public String getTrimmedEmailOrThrow(String email) {
        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        if (email == null || email.trim().isEmpty()) {
            fieldErrors.add(
                    new FieldErrorDto("email", "field.empty")
            );
            throw new FieldValidationException("login.error", fieldErrors);
        }
        return email.trim();
    }

    public void checkIfCodeIsValid(LoginSessionEntity session, String code) {
        if (code == null || code.isBlank() || !passwordEncoder.matches(code, session.getCode())) {
            throw new InvalidCodeException();
        }
    }

    public void ensureCodeIsNotExpired(LoginSessionEntity session) {
        if (session.getCodeExpires().before(new Timestamp(System.currentTimeMillis()))) {
            throw new CodeExpiredException();
        }
    }

    public LoginSessionEntity validateLoginSessionOrThrow(Optional<LoginSessionEntity> loginSessionOpt) {
        if (loginSessionOpt.isEmpty()) {
            throw new InvalidCodeException();
        }
        LoginSessionEntity loginSession = loginSessionOpt.get();

        if (loginSession.getAccountId() == null) {
            throw new InvalidCodeException();
        }
        return loginSession;
    }

    public String getTrimmedCodeOrThrow(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidCodeException();
        }
        return code.trim();
    }

    public void validateEmailFormatOrThrow(String email) {
        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        String emailPattern = "^[\\w-.]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$";
        if (email == null || !email.matches(emailPattern)) {
            fieldErrors.add(
                    new FieldErrorDto("email", "email.format.incorrect")
            );
            throw new FieldValidationException("login.error", fieldErrors);
        }
    }
}
