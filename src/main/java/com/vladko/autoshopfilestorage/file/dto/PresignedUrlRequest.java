package com.vladko.autoshopfilestorage.file.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PresignedUrlRequest(
        @Min(60)
        @Max(86400)
        Integer ttlSeconds
) {
}
