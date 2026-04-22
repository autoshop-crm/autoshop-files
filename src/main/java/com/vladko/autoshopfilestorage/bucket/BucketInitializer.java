package com.vladko.autoshopfilestorage.bucket;

import com.vladko.autoshopfilestorage.common.StorageUnavailableException;
import com.vladko.autoshopfilestorage.config.FileStorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BucketInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BucketInitializer.class);

    private final MinioClient minioClient;
    private final FileStorageProperties properties;

    public BucketInitializer(MinioClient minioClient, FileStorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.getMinio().isInitializeBuckets()) {
            return;
        }
        for (FileCategory category : FileCategory.values()) {
            ensureBucket(category.bucketName());
        }
    }

    private void ensureBucket(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket '{}'", bucketName);
            }
        } catch (Exception exception) {
            throw new StorageUnavailableException("Object storage is unavailable during bucket initialization", exception);
        }
    }
}
