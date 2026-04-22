package com.vladko.autoshopfilestorage.storage;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioClient minioClient;

    public MinioHealthIndicator(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public Health health() {
        try {
            boolean bucketAvailable = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(FileCategory.ORDER_DOCUMENT.bucketName())
                    .build());
            return bucketAvailable
                    ? Health.up().withDetail("bucket", FileCategory.ORDER_DOCUMENT.bucketName()).build()
                    : Health.down().withDetail("bucket", FileCategory.ORDER_DOCUMENT.bucketName()).build();
        } catch (Exception exception) {
            return Health.down().withDetail("storage", "minio").build();
        }
    }
}
