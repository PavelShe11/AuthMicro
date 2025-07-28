package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;

public abstract class AbstractException extends RuntimeException{
    private final String title;
    private final HttpStatus status;

    public AbstractException(String message, String title, HttpStatus status) {
        super(message);
        this.title = title;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
}
