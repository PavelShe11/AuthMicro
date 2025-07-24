package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CodeVerificationException extends RuntimeException {
    public CodeVerificationException(String message) {
        super(message);
    }
}
