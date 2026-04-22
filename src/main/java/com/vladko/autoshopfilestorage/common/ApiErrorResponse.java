package com.vladko.autoshopfilestorage.common;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldViolation> details
) {

    public record FieldViolation(String field, String message) {
    }
}
