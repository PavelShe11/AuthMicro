package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import io.github.pavelshe11.authmicro.api.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.exceptions.FieldValidationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RegistrationValidation {
    public void ensureCodeIsExpired(RegistrationSessionEntity session) {
        if (session.getCodeExpires().after(new Timestamp(System.currentTimeMillis()))) {
            throw new CodeVerificationException("error", "Код еще не истёк.");
        }
    }

    public void ensureCodeIsNotExpired(RegistrationSessionEntity session) {
        if (session.getCodeExpires().before(new Timestamp(System.currentTimeMillis()))) {
            throw new CodeVerificationException("error", "Код подтверждения истёк. Пожалуйста, запросите новый код.");
        }
    }

    public void checkIfCodeIsValid(String code, RegistrationSessionEntity registrationSession) {
        if (!(Objects.equals(code, registrationSession.getCode()))) {
            throw new InvalidCodeException("error", "Неверный код подтверждения.");
        }
    }

    public String getTrimmedEmail(String email) {
        return email.trim();
    }

    public void validateUserDataOrThrow(AccountValidatorProto.ValidateUserDataResponse accountValidatorResponse) {
        if (!accountValidatorResponse.getAccept()) {
            List<FieldErrorDto> fieldErrors = accountValidatorResponse.getDetailedErrorsList().stream()
                    .map(err -> new FieldErrorDto(err.getField(), err.getMessage()))
                    .toList();

            throw new FieldValidationException(fieldErrors);

        }
    }
}
