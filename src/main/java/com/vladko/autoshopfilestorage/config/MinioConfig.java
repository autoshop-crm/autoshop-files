package com.vladko.autoshopfilestorage.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Clock;

@Configuration
public class MinioConfig {

    @Bean
    MinioClient minioClient(FileStorageProperties properties) {
        FileStorageProperties.Minio minio = properties.getMinio();
        if (!StringUtils.hasText(minio.getEndpoint())
                || !StringUtils.hasText(minio.getAccessKey())
                || !StringUtils.hasText(minio.getSecretKey())) {
            throw new IllegalStateException("MinIO endpoint, access key and secret key must be provided through environment variables");
        }
        return MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
