package com.vladko.autoshopfilestorage.metadata;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.file.FileStatus;
import com.vladko.autoshopfilestorage.file.OwnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private FileCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private OwnerType ownerType;

    @Column(nullable = false, length = 128)
    private String ownerId;

    @Column(length = 128)
    private String uploadedBy;

    @Column(nullable = false, length = 128)
    private String bucketName;

    @Column(nullable = false, unique = true, length = 1024)
    private String objectKey;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 255)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 64)
    private String checksumSha256;

    @Column(length = 255)
    private String etag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FileStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;

    protected FileMetadata() {
    }

    public FileMetadata(
            UUID id,
            FileCategory category,
            OwnerType ownerType,
            String ownerId,
            String uploadedBy,
            String bucketName,
            String objectKey,
            String originalFilename,
            String contentType,
            long sizeBytes,
            String checksumSha256
    ) {
        this.id = id;
        this.category = category;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.uploadedBy = uploadedBy;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.checksumSha256 = checksumSha256;
        this.status = FileStatus.PENDING;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void markAvailable() {
        status = FileStatus.AVAILABLE;
    }

    public void markUploadFailed() {
        status = FileStatus.UPLOAD_FAILED;
    }

    public void markDeleted() {
        status = FileStatus.DELETED;
        deletedAt = Instant.now();
    }

    public boolean isAvailable() {
        return status == FileStatus.AVAILABLE;
    }

    public boolean isDeleted() {
        return status == FileStatus.DELETED;
    }

    public UUID getId() {
        return id;
    }

    public FileCategory getCategory() {
        return category;
    }

    public OwnerType getOwnerType() {
        return ownerType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public String getEtag() {
        return etag;
    }

    public FileStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
