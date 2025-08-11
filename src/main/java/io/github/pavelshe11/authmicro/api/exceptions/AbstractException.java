package io.github.pavelshe11.authmicro.api.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AbstractException extends RuntimeException {
    private final String messageCode;
    private final HttpStatus status;
    private final int errorCode;


    public AbstractException( String messageCode, HttpStatus status, int errorCode) {
        this.messageCode = messageCode;
        this.status = status;
        this.errorCode = errorCode;
    }

}
