package dev.viniciusjmr.servicerequest.domain.exception;

public class ForbidenOperationException extends RuntimeException {
    public ForbidenOperationException(String message) {
        super(message);
    }
}
