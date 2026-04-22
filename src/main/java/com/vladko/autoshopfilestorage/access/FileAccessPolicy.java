package com.vladko.autoshopfilestorage.access;

import com.vladko.autoshopfilestorage.metadata.FileMetadata;

public interface FileAccessPolicy {

    void assertCanUpload(FileUploadContext context);

    void assertCanRead(FileMetadata metadata);

    void assertCanDelete(FileMetadata metadata);
}
