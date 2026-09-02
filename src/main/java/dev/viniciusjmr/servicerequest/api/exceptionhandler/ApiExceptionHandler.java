package dev.viniciusjmr.servicerequest.api.exceptionhandler;

import dev.viniciusjmr.servicerequest.api.model.exception.GlobalExceptionModel;
import dev.viniciusjmr.servicerequest.domain.exception.FieldException;
import dev.viniciusjmr.servicerequest.domain.exception.ForbidenOperationException;
import dev.viniciusjmr.servicerequest.domain.exception.ResourceAlreadyExistsException;
import dev.viniciusjmr.servicerequest.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        if (body instanceof GlobalExceptionModel) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request);
        }

        GlobalExceptionModel exceptionModel = new GlobalExceptionModel(
                ex.getMessage() != null ? ex.getMessage() : "Erro inesperado",
                statusCode.value(),
                Instant.now(),
                null
        );

        return super.handleExceptionInternal(ex, exceptionModel, headers, statusCode, request);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Object> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex,
            WebRequest request
    ) {
        var code = HttpStatus.CONFLICT;

        var body = new GlobalExceptionModel(
                ex.getMessage(),
                code.value(),
                Instant.now(),
                null
        );

        return handleExceptionInternal(ex, body, new HttpHeaders(), code, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleFieldException(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        var code = HttpStatus.NOT_FOUND;

        var body = new GlobalExceptionModel(
                ex.getMessage(),
                code.value(),
                Instant.now(),
                null
        );

        return handleExceptionInternal(ex, body, new HttpHeaders(), code, request);
    }

    @ExceptionHandler(ForbidenOperationException.class)
    public ResponseEntity<Object> handleFieldException(
            ForbidenOperationException ex,
            WebRequest request
    ) {
        var code = HttpStatus.FORBIDDEN;

        var body = new GlobalExceptionModel(
                ex.getMessage(),
                code.value(),
                Instant.now(),
                null
        );

        return handleExceptionInternal(ex, body, new HttpHeaders(), code, request);
    }

    @ExceptionHandler(FieldException.class)
    public ResponseEntity<Object> handleFieldException(
            FieldException ex,
            WebRequest request
    ) {
        var code = HttpStatus.BAD_REQUEST;

        var body = new GlobalExceptionModel(
                ex.getMessage(),
                code.value(),
                Instant.now(),
                ex.getFields()
        );

        return handleExceptionInternal(ex, body, new HttpHeaders(), code, request);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        new FieldException.Field(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                )
                .toList();

        var code = HttpStatus.BAD_REQUEST;

        var body = new GlobalExceptionModel(
                ex.getMessage(),
                code.value(),
                Instant.now(),
                errors
        );

        return handleExceptionInternal(ex, body, headers, code, request);
    }
}
