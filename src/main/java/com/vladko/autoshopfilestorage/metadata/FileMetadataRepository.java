package com.vladko.autoshopfilestorage.metadata;

import com.vladko.autoshopfilestorage.file.FileStatus;
import com.vladko.autoshopfilestorage.file.OwnerType;
import com.vladko.autoshopfilestorage.bucket.FileCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    Optional<FileMetadata> findByIdAndStatus(UUID id, FileStatus status);

    Page<FileMetadata> findByOwnerTypeAndOwnerIdAndStatusOrderByCreatedAtDesc(
            OwnerType ownerType,
            String ownerId,
            FileStatus status,
            Pageable pageable
    );

    Page<FileMetadata> findByOwnerTypeAndOwnerIdAndCategoryAndStatusOrderByCreatedAtDesc(
            OwnerType ownerType,
            String ownerId,
            FileCategory category,
            FileStatus status,
            Pageable pageable
    );

    Page<FileMetadata> findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(OwnerType ownerType, String ownerId, Pageable pageable);

    Page<FileMetadata> findByOwnerTypeAndOwnerIdAndCategoryOrderByCreatedAtDesc(
            OwnerType ownerType,
            String ownerId,
            FileCategory category,
            Pageable pageable
    );
}
