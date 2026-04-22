package com.vladko.autoshopfilestorage.file.dto;

import java.time.Instant;
import java.util.UUID;

public record PresignedUrlResponse(
        UUID fileId,
        String url,
        Instant expiresAt,
        int expiresInSeconds
) {
}
