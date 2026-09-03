package dev.viniciusjmr.servicerequest.api.model.solicitations;

import java.util.List;

public record SearchPageResponse<T>(
        List<T> items,
        int page,
        int size,
        long total
) {
}
