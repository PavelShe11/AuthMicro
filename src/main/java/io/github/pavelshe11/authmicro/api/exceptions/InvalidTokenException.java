package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AbstractException {
    public InvalidTokenException(String title, String message) {
        super(title, message, HttpStatus.UNAUTHORIZED);
    }
}
