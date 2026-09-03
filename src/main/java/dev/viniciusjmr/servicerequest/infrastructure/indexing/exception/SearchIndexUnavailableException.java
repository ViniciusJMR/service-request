package dev.viniciusjmr.servicerequest.infrastructure.indexing.exception;

public class SearchIndexUnavailableException extends RuntimeException {
    public SearchIndexUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchIndexUnavailableException(String message) {
        super(message);
    }
}
