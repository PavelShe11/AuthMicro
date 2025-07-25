package io.github.pavelshe11.authmicro.api.exceptions;

import io.github.pavelshe11.authmicro.api.dto.ErrorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RequiredArgsConstructor
@RestControllerAdvice
public class CustomErrorController implements ErrorController {

    @ExceptionHandler(CodeVerificationException.class)
    public ResponseEntity<ErrorDto> handleCodeExpired(CodeVerificationException ex) {
        return ResponseEntity.badRequest().body(
                ErrorDto.builder()
                        .error("Ошибка времени действия кода.")
                        .errorDescription(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ErrorDto> handleInvalidCode(InvalidCodeException ex) {
        return ResponseEntity.badRequest().body(
                ErrorDto.builder()
                        .error("Неверный код.")
                        .errorDescription(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(ServerAnswerException.class)
    public ResponseEntity<ErrorDto> handleServerError(ServerAnswerException ex) {
        return ResponseEntity.status(500).body(
                ErrorDto.builder()
                        .error("Ошибка сервера.")
                        .errorDescription(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDto> handleBadRequest(BadRequestException ex) {
        return ResponseEntity
                .badRequest()
                .body(ErrorDto.builder()
                        .error("Ошибка регистрации.")
                        .errorDescription(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder detailed = new StringBuilder();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                detailed.append(error.getField())
                        .append(": ")
                        .append(error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(
                ErrorDto.builder()
                        .error("Некорректное заполнение поля.")
                        .errorDescription(detailed.toString())
                        .build()
        );
    }
}