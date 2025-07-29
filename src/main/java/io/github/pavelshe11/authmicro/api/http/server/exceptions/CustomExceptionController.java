package io.github.pavelshe11.authmicro.api.http.server.exceptions;

import io.github.pavelshe11.authmicro.api.http.server.dto.ErrorDto;
import io.github.pavelshe11.authmicro.api.http.server.dto.FieldErrorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@RestControllerAdvice
public class CustomExceptionController {

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ErrorDto> handleFieldValidationExceptions(FieldValidationException ex) {
        ErrorDto response = new ErrorDto("Ошибка регистрации", ex.getErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGeneralExceptions(Exception ex) {
        ErrorDto response = ErrorDto.builder()
                .error(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(AbstractException.class)
    public ResponseEntity<Map<String, String>> handleAbstractExceptions(AbstractException ex) {
        Map<String, String> errorBody = new HashMap<>();

        errorBody.put("error", ex.getTitle());

        return ResponseEntity
                .status(ex.getStatus())
                .body(errorBody);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex
    ) {
        List<FieldErrorDto> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDto(error.getField(),
                        error.getDefaultMessage()))
                .toList();

        ErrorDto response = new ErrorDto("Ошибка регистрации", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}