package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidCodeException extends AbstractException {

    public InvalidCodeException() {
        super("error.invalid.code", HttpStatus.UNAUTHORIZED);
    }
}

