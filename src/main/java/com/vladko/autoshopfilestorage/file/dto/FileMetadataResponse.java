package com.vladko.autoshopfilestorage.file.dto;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.file.FileStatus;
import com.vladko.autoshopfilestorage.file.OwnerType;

import java.time.Instant;
import java.util.UUID;

public record FileMetadataResponse(
        UUID id,
        FileCategory category,
        OwnerType ownerType,
        String ownerId,
        String uploadedBy,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String checksumSha256,
        FileStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
