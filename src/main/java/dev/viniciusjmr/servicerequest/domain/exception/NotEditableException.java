package dev.viniciusjmr.servicerequest.domain.exception;

public class NotEditableException extends RuntimeException {
    public NotEditableException(String message) {
        super(message);
    }
}
