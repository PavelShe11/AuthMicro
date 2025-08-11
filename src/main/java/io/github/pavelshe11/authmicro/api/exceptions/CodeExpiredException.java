package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;

public class CodeExpiredException extends AbstractException {
    public CodeExpiredException() {
        super("error.code.expired", HttpStatus.BAD_REQUEST, 400);
    }
}
