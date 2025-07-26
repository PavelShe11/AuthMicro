package io.github.pavelshe11.authmicro.api.exceptions;

public class BadRequestException extends AbstractException {
    public BadRequestException(String title, String message) {
        super(title, message);
    }
}