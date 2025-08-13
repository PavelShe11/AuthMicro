package io.github.pavelshe11.authmicro.api.exceptions;

import org.springframework.http.HttpStatus;

public class ServerAnswerException extends AbstractException {
    public ServerAnswerException() {
        super("error.server.error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
