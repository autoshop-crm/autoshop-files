package com.vladko.autoshopfilestorage.storage;

import com.vladko.autoshopfilestorage.common.StorageUnavailableException;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;

    public MinioObjectStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void upload(String bucket, String objectKey, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .contentType(contentType)
                    .stream(inputStream, size, -1)
                    .build());
        } catch (Exception exception) {
            throw new StorageUnavailableException("Object storage upload failed", exception);
        }
    }

    @Override
    public StoredObject download(String bucket, String objectKey, String contentType, long size) {
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            return new StoredObject(inputStream, contentType, size);
        } catch (Exception exception) {
            throw new StorageUnavailableException("Object storage download failed", exception);
        }
    }

    @Override
    public void remove(String bucket, String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception exception) {
            throw new StorageUnavailableException("Object storage delete failed", exception);
        }
    }

    @Override
    public String presignedGetUrl(String bucket, String objectKey, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(expirySeconds)
                    .build());
        } catch (Exception exception) {
            throw new StorageUnavailableException("Object storage presigned URL generation failed", exception);
        }
    }
}
