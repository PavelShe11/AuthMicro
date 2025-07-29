package io.github.pavelshe11.authmicro.validators;

import io.github.pavelshe11.authmicro.api.http.server.dto.FieldErrorDto;
import io.github.pavelshe11.authmicro.api.http.server.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.http.server.exceptions.FieldValidationException;
import io.github.pavelshe11.authmicro.api.http.server.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoginValidation {
    private final LoginSessionRepository loginSessionRepository;
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


    public void checkIfCodeIsValid(LoginSessionEntity session, String code) {
        if (!(Objects.equals(code, session.getCode()))) {
            throw new InvalidCodeException("error", "Неверный код подтверждения.");
        }
    }

    public void ensureCodeIsNotExpired(LoginSessionEntity session) {
        if (session.getCodeExpires().isBefore(Instant.now())) {
            throw new CodeVerificationException("error", "Код подтверждения истёк. Пожалуйста, запросите новый код.");
        }
    }
}
