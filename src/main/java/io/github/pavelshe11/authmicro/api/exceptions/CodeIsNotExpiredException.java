package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;

public class CodeIsNotExpiredException extends AbstractException {
    public CodeIsNotExpiredException() {
        super("error.code.not.expired", HttpStatus.BAD_REQUEST, 400);
    }
}
