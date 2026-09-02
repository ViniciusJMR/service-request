package dev.viniciusjmr.servicerequest.domain.exception;

import java.util.List;

public class FieldException extends RuntimeException {
    private List<Field> fields;

    public FieldException(String message, List<Field> fields) {
        super(message);
        this.fields = fields;
    }

    public List<Field> getFields() {
        return fields;
    }

    public record Field (
            String field,
            String message
    ){}
}
