package com.vladko.autoshopfilestorage.presign;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.config.FileStorageProperties;
import com.vladko.autoshopfilestorage.file.OwnerType;
import com.vladko.autoshopfilestorage.file.dto.PresignedUrlResponse;
import com.vladko.autoshopfilestorage.metadata.FileMetadata;
import com.vladko.autoshopfilestorage.storage.ObjectStorageService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PresignServiceTest {

    private final ObjectStorageService storage = mock(ObjectStorageService.class);
    private final PresignService service = new PresignService(
            storage,
            new FileStorageProperties(),
            Clock.fixed(Instant.parse("2026-04-20T10:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void clampsRequestedExpiryToConfiguredMaximum() {
        UUID fileId = UUID.randomUUID();
        FileMetadata metadata = new FileMetadata(
                fileId,
                FileCategory.ORDER_DOCUMENT,
                OwnerType.ORDER,
                "42",
                "user-1",
                "documents",
                "order-document/2026/04/20/%s/file-checksum.pdf".formatted(fileId),
                "file.pdf",
                "application/pdf",
                4,
                "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        );
        when(storage.presignedGetUrl("documents", "order-document/2026/04/20/%s/file-checksum.pdf".formatted(fileId), 3600))
                .thenReturn("http://localhost:9000/documents/document/order/42/file.pdf");

        PresignedUrlResponse response = service.downloadUrl(fileId, metadata, 7200);

        assertThat(response.expiresInSeconds()).isEqualTo(3600);
        assertThat(response.expiresAt()).isEqualTo(Instant.parse("2026-04-20T11:00:00Z"));
    }
}
