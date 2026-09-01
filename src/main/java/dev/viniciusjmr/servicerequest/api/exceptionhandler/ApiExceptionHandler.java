package dev.viniciusjmr.servicerequest.api.exceptionhandler;

import dev.viniciusjmr.servicerequest.api.model.exception.GlobalExceptionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
                Instant.now()
        );

        return super.handleExceptionInternal(ex, exceptionModel, headers, statusCode, request);
    }



}
