package io.github.pavelshe11.authmicro.api.exceptions;

public abstract class AbstractException extends RuntimeException{
    private final String title;

    public AbstractException(String message, String title) {
        super(message);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
