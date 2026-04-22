package com.vladko.autoshopfilestorage.storage;

import java.io.InputStream;

public interface ObjectStorageService {

    void upload(String bucket, String objectKey, InputStream inputStream, long size, String contentType);

    StoredObject download(String bucket, String objectKey, String contentType, long size);

    void remove(String bucket, String objectKey);

    String presignedGetUrl(String bucket, String objectKey, int expirySeconds);
}
