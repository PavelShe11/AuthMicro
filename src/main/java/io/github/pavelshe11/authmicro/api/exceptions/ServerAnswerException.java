package io.github.pavelshe11.authmicro.api.exceptions;

public class ServerAnswerException extends RuntimeException {
    public ServerAnswerException() {
        super("Внутренняя ошибка сервера");
    }
}
