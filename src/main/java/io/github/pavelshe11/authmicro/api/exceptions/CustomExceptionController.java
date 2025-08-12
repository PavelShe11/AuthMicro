package io.github.pavelshe11.authmicro.api.exceptions;

import io.github.pavelshe11.authmicro.api.dto.ErrorDto;
import io.github.pavelshe11.authmicro.api.dto.FieldErrorDto;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


@RequiredArgsConstructor
@RestControllerAdvice
public class CustomExceptionController {
    private final MessageSource messageSource;

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ErrorDto> handleFieldValidationExceptions(FieldValidationException ex) {

        String errorMessage = messageSource.getMessage(
                ex.getMessage(),
                null,
                LocaleContextHolder.getLocale()
        );

        List<FieldErrorDto> errors = ex.getErrors().stream()
                .map(error -> new FieldErrorDto(
                        error.getField(),
                        resolveMessage(error.getMessage())
                )).toList();

        ErrorDto response = ErrorDto.builder()
                .error(errorMessage)
                .detailedErrors(errors)
                .build();

//        ErrorDto response = new ErrorDto(errorMessage, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGeneralExceptions(Exception ex) {
        System.out.println(ex.getMessage());
        ErrorDto response = ErrorDto.builder()
                .error(messageSource.getMessage("server.inner.error", null, LocaleContextHolder.getLocale()))
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(AbstractException.class)
    public ResponseEntity<ErrorDto> handleAbstractExceptions(AbstractException ex) {
        String errorMessage = messageSource.getMessage(
                ex.getMessageCode(),

                null,
                LocaleContextHolder.getLocale()
        );

        ErrorDto response = ErrorDto.builder()
                .error(errorMessage)
                .build();

        return ResponseEntity
                .status(ex.getStatus())
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String uri = request.getRequestURI();

        String contextMessage;
        if (uri.contains("/login")) {
            contextMessage = messageSource.getMessage("login.error", null, LocaleContextHolder.getLocale());
        } else if (uri.contains("/registration")) {
            contextMessage = messageSource.getMessage("registration.error", null, LocaleContextHolder.getLocale());
        } else {
            contextMessage = messageSource.getMessage("validation.error", null, LocaleContextHolder.getLocale());
        }

        List<FieldErrorDto> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDto(error.getField(),
                        error.getDefaultMessage()))
                .toList();

        ErrorDto response = ErrorDto.builder()
                .error(contextMessage)
                .detailedErrors(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String resolveMessage(String codeOrMessage) {
        try {
            return messageSource.getMessage(codeOrMessage, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return codeOrMessage;
        }
    }

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ErrorDto> handleGrpcException(StatusRuntimeException ex) {
        ErrorDto response = ErrorDto.builder()
                .error(messageSource.getMessage("server.inner.error", null, LocaleContextHolder.getLocale()))
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}

