package dev.viniciusjmr.servicerequest.domain.exception;

public class InvalidOperation extends RuntimeException{
    public InvalidOperation(String message) {
        super(message);
    }
}
