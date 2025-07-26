package io.github.pavelshe11.authmicro.api.exceptions;

public class CodeVerificationException extends AbstractException {
    public CodeVerificationException(String title, String message) {
        super(title, message);
    }
}
