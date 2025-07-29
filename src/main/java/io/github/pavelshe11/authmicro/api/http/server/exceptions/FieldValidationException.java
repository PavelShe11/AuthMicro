package io.github.pavelshe11.authmicro.api.http.server.exceptions;

import io.github.pavelshe11.authmicro.api.http.server.dto.FieldErrorDto;

import java.util.List;

public class FieldValidationException extends RuntimeException{
    private final List<FieldErrorDto> errors;

    public FieldValidationException(List<FieldErrorDto> errors) {
        super("Ошибка регистрации");
        this.errors = errors;
    }

    public List<FieldErrorDto> getErrors() {
        return errors;
    }

}
