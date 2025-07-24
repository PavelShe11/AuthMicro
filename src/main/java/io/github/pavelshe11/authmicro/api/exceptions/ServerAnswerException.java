package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ServerAnswerException extends RuntimeException {
    public ServerAnswerException(String message) {
        super(message); // вызов конструктора RuntimeException(String message)
    }
}
