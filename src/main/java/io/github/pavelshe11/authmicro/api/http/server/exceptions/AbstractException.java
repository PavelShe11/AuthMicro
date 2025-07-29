package io.github.pavelshe11.authmicro.api.http.server.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AbstractException extends RuntimeException{
    private final String title;
    private final HttpStatus status;

    public AbstractException(String message, String title, HttpStatus status) {
        super(message);
        this.title = title;
        this.status = status;
    }

}
