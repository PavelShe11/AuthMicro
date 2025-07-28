package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidCodeException extends AbstractException {
    public InvalidCodeException(String title, String message) {
        super(title, message, HttpStatus.BAD_REQUEST);
    }
}

