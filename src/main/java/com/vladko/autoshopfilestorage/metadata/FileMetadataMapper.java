package com.vladko.autoshopfilestorage.metadata;

import com.vladko.autoshopfilestorage.file.dto.FileMetadataResponse;

public final class FileMetadataMapper {

    private FileMetadataMapper() {
    }

    public static FileMetadataResponse toResponse(FileMetadata metadata) {
        return new FileMetadataResponse(
                metadata.getId(),
                metadata.getCategory(),
                metadata.getOwnerType(),
                metadata.getOwnerId(),
                metadata.getUploadedBy(),
                metadata.getOriginalFilename(),
                metadata.getContentType(),
                metadata.getSizeBytes(),
                metadata.getChecksumSha256(),
                metadata.getStatus(),
                metadata.getCreatedAt(),
                metadata.getUpdatedAt(),
                metadata.getDeletedAt()
        );
    }
}
