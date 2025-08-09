package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import io.github.pavelshe11.authmicro.api.exceptions.CodeExpiredException;
import io.github.pavelshe11.authmicro.api.exceptions.FieldValidationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RegistrationValidation {
    private final PasswordEncoder passwordEncoder;

    public boolean isCodeExpired(RegistrationSessionEntity session) {
        if (session.getCodeExpires().after(new Timestamp(System.currentTimeMillis()))) {
            return false;
        }
        return true;
    }

    public void ensureCodeIsNotExpired(RegistrationSessionEntity session) {
        if (session.getCodeExpires().before(new Timestamp(System.currentTimeMillis()))) {
            throw new CodeExpiredException();
        }
    }

    public void checkIfCodeIsValid(String code, RegistrationSessionEntity registrationSession) {
        if (code == null || code.isBlank() || !passwordEncoder.matches(code, registrationSession.getCode())) {
            throw new InvalidCodeException();
        }
    }

    public String getTrimmedEmailOrThrow(String email) {
        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        if (email == null || email.trim().isEmpty()) {
            fieldErrors.add(
                    new FieldErrorDto("email", "field.empty")
            );
            throw new FieldValidationException("registration.error", fieldErrors);
        }
        return email.trim();
    }

    public void validateUserDataOrThrow(AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse) {
        if (!accountValidatorResponse.getAccept()) {
            List<FieldErrorDto> fieldErrors = accountValidatorResponse.getDetailedErrorsList().stream()
                    .map(err -> new FieldErrorDto(err.getField(), err.getMessage()))
                    .toList();

            throw new FieldValidationException("registration.error", fieldErrors);

        }
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
            throw new FieldValidationException("registration.error", fieldErrors);
        }
    }
}
