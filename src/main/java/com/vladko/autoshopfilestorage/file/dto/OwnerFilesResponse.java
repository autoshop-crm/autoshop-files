package com.vladko.autoshopfilestorage.file.dto;

import java.util.List;

public record OwnerFilesResponse(
        List<FileMetadataResponse> items,
        int page,
        int size,
        long totalElements
) {
}
