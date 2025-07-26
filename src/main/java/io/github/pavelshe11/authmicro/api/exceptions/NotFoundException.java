package io.github.pavelshe11.authmicro.api.exceptions;

public class NotFoundException extends AbstractException {
    public NotFoundException(String title, String message) {
        super(title, message);
    }
}