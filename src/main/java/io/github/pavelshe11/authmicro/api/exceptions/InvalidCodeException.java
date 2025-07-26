package io.github.pavelshe11.authmicro.api.exceptions;

public class InvalidCodeException extends AbstractException {
    public InvalidCodeException(String title, String message) {
        super(title, message);
    }
}

