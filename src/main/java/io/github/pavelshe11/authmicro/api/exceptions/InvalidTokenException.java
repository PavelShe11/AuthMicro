package io.github.pavelshe11.authmicro.api.exceptions;

public class InvalidTokenException extends AbstractException {
    public InvalidTokenException(String title, String message) {
        super(title, message);
    }
}
