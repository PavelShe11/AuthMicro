package io.github.pavelshe11.authmicro.api.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AbstractException extends RuntimeException {
    private final String messageCode;
    private final HttpStatus status;

    public AbstractException( String messageCode, HttpStatus status) {
        this.messageCode = messageCode;
        this.status = status;
    }

}
