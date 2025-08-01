package io.github.pavelshe11.authmicro.api.exceptions;

public class ServerAnswerException extends RuntimeException {
    public ServerAnswerException(String message) {
        super("Сервер не отвечает");
    }
}
