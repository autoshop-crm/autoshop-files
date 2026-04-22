package com.vladko.autoshopfilestorage.metadata;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.file.FileStatus;
import com.vladko.autoshopfilestorage.file.OwnerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FileMetadataRepositoryTest {

    @Autowired
    private FileMetadataRepository repository;

    @Test
    void storesAndSoftDeletesMetadata() {
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
        metadata.markAvailable();
        repository.saveAndFlush(metadata);

        assertThat(repository.findByIdAndStatus(fileId, FileStatus.AVAILABLE)).isPresent();
        assertThat(repository.findByOwnerTypeAndOwnerIdAndStatusOrderByCreatedAtDesc(
                OwnerType.ORDER,
                "42",
                FileStatus.AVAILABLE,
                org.springframework.data.domain.PageRequest.of(0, 20)
        )).hasSize(1);

        metadata.markDeleted();
        repository.saveAndFlush(metadata);

        assertThat(repository.findByIdAndStatus(fileId, FileStatus.AVAILABLE)).isEmpty();
        assertThat(repository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(
                OwnerType.ORDER,
                "42",
                org.springframework.data.domain.PageRequest.of(0, 20)
        ))
                .singleElement()
                .extracting(FileMetadata::getStatus)
                .isEqualTo(FileStatus.DELETED);
    }
}
