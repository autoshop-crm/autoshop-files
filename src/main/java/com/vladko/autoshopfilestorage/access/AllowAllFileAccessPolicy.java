package com.vladko.autoshopfilestorage.access;

import com.vladko.autoshopfilestorage.metadata.FileMetadata;
import org.springframework.stereotype.Component;

@Component
public class AllowAllFileAccessPolicy implements FileAccessPolicy {

    @Override
    public void assertCanUpload(FileUploadContext context) {
    }

    @Override
    public void assertCanRead(FileMetadata metadata) {
    }

    @Override
    public void assertCanDelete(FileMetadata metadata) {
    }
}
