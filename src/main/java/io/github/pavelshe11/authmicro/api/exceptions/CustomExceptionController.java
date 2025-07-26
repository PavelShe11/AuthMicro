package io.github.pavelshe11.authmicro.api.exceptions;

import io.github.pavelshe11.authmicro.api.dto.ErrorDto;
import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


@RequiredArgsConstructor
@RestControllerAdvice
public class CustomExceptionController {

    @ExceptionHandler(AbstractException.class)
    public ResponseEntity<ErrorDto> handleAbstractException(AbstractException ex) {
        return ResponseEntity.badRequest().body(
                ErrorDto.builder()
                        .error(ex.getTitle())
                        .errorDescription(ex.getMessage())
                        .build()
        );
    }
    @ExceptionHandler(ServerAnswerException.class)
    public ResponseEntity<ErrorDto> handleServerError(ServerAnswerException ex) {
        return ResponseEntity.status(500).body(
                ErrorDto.builder()
                        .error(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDto(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(
                ErrorDto.builder()
                        .error("Ошибка регистрации")
                        .detailedErrors(fieldErrors)
                        .build()
        );
    }
}