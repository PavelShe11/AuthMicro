package io.github.pavelshe11.authmicro.api.exceptions;

import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import lombok.Getter;

import java.util.List;

@Getter
public class FieldValidationException extends RuntimeException {
    private final List<FieldErrorDto> errors;
    private final String message;

    public FieldValidationException(String message, List<FieldErrorDto> errors) {
        this.message = message;
        this.errors = errors;
    }

}
