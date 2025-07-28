package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidInstituteException extends AbstractException{
    public InvalidInstituteException(String title, String message) {
        super(title, message, HttpStatus.BAD_REQUEST);
    }
}
