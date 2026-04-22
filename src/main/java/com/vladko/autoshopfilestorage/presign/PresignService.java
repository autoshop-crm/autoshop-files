package com.vladko.autoshopfilestorage.presign;

import com.vladko.autoshopfilestorage.common.InvalidFileException;
import com.vladko.autoshopfilestorage.config.FileStorageProperties;
import com.vladko.autoshopfilestorage.file.dto.PresignedUrlResponse;
import com.vladko.autoshopfilestorage.metadata.FileMetadata;
import com.vladko.autoshopfilestorage.storage.ObjectStorageService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class PresignService {

    private final ObjectStorageService objectStorageService;
    private final FileStorageProperties properties;
    private final Clock clock;

    public PresignService(ObjectStorageService objectStorageService, FileStorageProperties properties, Clock clock) {
        this.objectStorageService = objectStorageService;
        this.properties = properties;
        this.clock = clock;
    }

    public PresignedUrlResponse downloadUrl(UUID fileId, FileMetadata metadata, Integer requestedExpirySeconds) {
        int expirySeconds = clampExpiry(requestedExpirySeconds);
        String url = objectStorageService.presignedGetUrl(metadata.getBucketName(), metadata.getObjectKey(), expirySeconds);
        return new PresignedUrlResponse(fileId, url, Instant.now(clock).plusSeconds(expirySeconds), expirySeconds);
    }

    private int clampExpiry(Integer requestedExpirySeconds) {
        int defaultExpiry = properties.getPresign().getDefaultExpirySeconds();
        int maxExpiry = properties.getPresign().getMaxExpirySeconds();
        if (requestedExpirySeconds == null) {
            return defaultExpiry;
        }
        if (requestedExpirySeconds < 60) {
            throw new InvalidFileException("Presigned URL expiry must be at least 60 seconds");
        }
        return Math.min(requestedExpirySeconds, maxExpiry);
    }
}
