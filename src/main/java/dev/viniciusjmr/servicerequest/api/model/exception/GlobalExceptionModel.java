package dev.viniciusjmr.servicerequest.api.model.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;

public record GlobalExceptionModel(
        String error,
        Integer status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT-3")
        Instant timestamp
) { }
