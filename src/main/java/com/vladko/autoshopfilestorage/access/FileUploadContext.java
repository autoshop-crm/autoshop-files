package com.vladko.autoshopfilestorage.access;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.file.OwnerType;

public record FileUploadContext(
        FileCategory category,
        OwnerType ownerType,
        String ownerId,
        String uploadedBy
) {
}
