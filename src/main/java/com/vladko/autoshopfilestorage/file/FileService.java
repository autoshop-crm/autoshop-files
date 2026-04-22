package com.vladko.autoshopfilestorage.file;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.access.FileAccessPolicy;
import com.vladko.autoshopfilestorage.access.FileUploadContext;
import com.vladko.autoshopfilestorage.common.DeletedFileException;
import com.vladko.autoshopfilestorage.common.FileNotFoundException;
import com.vladko.autoshopfilestorage.common.FileNotAvailableException;
import com.vladko.autoshopfilestorage.common.InvalidFileException;
import com.vladko.autoshopfilestorage.common.StorageUnavailableException;
import com.vladko.autoshopfilestorage.file.dto.FileMetadataResponse;
import com.vladko.autoshopfilestorage.file.dto.OwnerFilesResponse;
import com.vladko.autoshopfilestorage.file.dto.PresignedUrlResponse;
import com.vladko.autoshopfilestorage.metadata.FileMetadata;
import com.vladko.autoshopfilestorage.metadata.FileMetadataMapper;
import com.vladko.autoshopfilestorage.metadata.FileMetadataRepository;
import com.vladko.autoshopfilestorage.presign.PresignService;
import com.vladko.autoshopfilestorage.storage.ObjectStorageService;
import com.vladko.autoshopfilestorage.storage.StoredObject;
import com.vladko.autoshopfilestorage.validation.FileUploadValidator;
import com.vladko.autoshopfilestorage.validation.ObjectKeyFactory;
import com.vladko.autoshopfilestorage.validation.ValidatedUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileMetadataRepository repository;
    private final ObjectStorageService objectStorageService;
    private final FileUploadValidator fileUploadValidator;
    private final ObjectKeyFactory objectKeyFactory;
    private final PresignService presignService;
    private final FileAccessPolicy accessPolicy;

    public FileService(
            FileMetadataRepository repository,
            ObjectStorageService objectStorageService,
            FileUploadValidator fileUploadValidator,
            ObjectKeyFactory objectKeyFactory,
            PresignService presignService,
            FileAccessPolicy accessPolicy
    ) {
        this.repository = repository;
        this.objectStorageService = objectStorageService;
        this.fileUploadValidator = fileUploadValidator;
        this.objectKeyFactory = objectKeyFactory;
        this.presignService = presignService;
        this.accessPolicy = accessPolicy;
    }

    @Transactional(noRollbackFor = StorageUnavailableException.class)
    public FileMetadataResponse upload(
            FileCategory category,
            OwnerType ownerType,
            String ownerId,
            String uploadedBy,
            MultipartFile file
    ) {
        validateOwnerId(ownerId);
        accessPolicy.assertCanUpload(new FileUploadContext(category, ownerType, ownerId, normalizeOptional(uploadedBy)));
        ValidatedUpload validatedUpload = fileUploadValidator.validate(category, file);
        UUID fileId = UUID.randomUUID();
        String bucket = category.bucketName();
        String checksum = sha256(file);
        String objectKey = objectKeyFactory.create(category, fileId, validatedUpload.sanitizedFilename(), checksum);
        FileMetadata metadata = new FileMetadata(
                fileId,
                category,
                ownerType,
                ownerId,
                normalizeOptional(uploadedBy),
                bucket,
                objectKey,
                validatedUpload.sanitizedFilename(),
                validatedUpload.contentType(),
                validatedUpload.size(),
                checksum
        );
        repository.saveAndFlush(metadata);

        try {
            try (InputStream inputStream = file.getInputStream()) {
                objectStorageService.upload(bucket, objectKey, inputStream, validatedUpload.size(), validatedUpload.contentType());
            }
            metadata.markAvailable();
            return FileMetadataMapper.toResponse(repository.saveAndFlush(metadata));
        } catch (StorageUnavailableException exception) {
            metadata.markUploadFailed();
            repository.saveAndFlush(metadata);
            cleanupUploadedObject(bucket, objectKey);
            throw exception;
        } catch (Exception exception) {
            cleanupUploadedObject(bucket, objectKey);
            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new StorageUnavailableException("File upload failed", exception);
        }
    }

    @Transactional(readOnly = true)
    public FileMetadataResponse getMetadata(UUID fileId) {
        return repository.findById(fileId)
                .map(FileMetadataMapper::toResponse)
                .orElseThrow(() -> new FileNotFoundException(fileId));
    }

    @Transactional(readOnly = true)
    public OwnerFilesResponse listByOwner(
            OwnerType ownerType,
            String ownerId,
            FileCategory category,
            boolean includeDeleted,
            int page,
            int size
    ) {
        validateOwnerId(ownerId);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<FileMetadata> metadata = switch (queryMode(category, includeDeleted)) {
            case ALL_WITH_CATEGORY -> repository.findByOwnerTypeAndOwnerIdAndCategoryOrderByCreatedAtDesc(ownerType, ownerId, category, pageRequest);
            case AVAILABLE_WITH_CATEGORY -> repository.findByOwnerTypeAndOwnerIdAndCategoryAndStatusOrderByCreatedAtDesc(
                    ownerType,
                    ownerId,
                    category,
                    FileStatus.AVAILABLE,
                    pageRequest
            );
            case ALL -> repository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(ownerType, ownerId, pageRequest);
            case AVAILABLE -> repository.findByOwnerTypeAndOwnerIdAndStatusOrderByCreatedAtDesc(
                    ownerType,
                    ownerId,
                    FileStatus.AVAILABLE,
                    pageRequest
            );
        };
        return new OwnerFilesResponse(
                metadata.stream().map(FileMetadataMapper::toResponse).toList(),
                metadata.getNumber(),
                metadata.getSize(),
                metadata.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public StoredObject download(UUID fileId) {
        FileMetadata metadata = availableMetadata(fileId);
        accessPolicy.assertCanRead(metadata);
        return objectStorageService.download(
                metadata.getBucketName(),
                metadata.getObjectKey(),
                metadata.getContentType(),
                metadata.getSizeBytes()
        );
    }

    @Transactional(readOnly = true)
    public FileMetadata availableMetadata(UUID fileId) {
        FileMetadata metadata = repository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        if (metadata.isDeleted()) {
            throw new DeletedFileException(fileId);
        }
        if (!metadata.isAvailable()) {
            throw new FileNotAvailableException(fileId, metadata.getStatus().name());
        }
        return metadata;
    }

    @Transactional(readOnly = true)
    public PresignedUrlResponse presignedDownloadUrl(UUID fileId, Integer requestedExpirySeconds) {
        FileMetadata metadata = availableMetadata(fileId);
        accessPolicy.assertCanRead(metadata);
        return presignService.downloadUrl(fileId, metadata, requestedExpirySeconds);
    }

    @Transactional
    public void delete(UUID fileId) {
        FileMetadata metadata = repository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        if (metadata.isDeleted()) {
            return;
        }
        accessPolicy.assertCanDelete(metadata);
        if (metadata.isAvailable()) {
            objectStorageService.remove(metadata.getBucketName(), metadata.getObjectKey());
        }
        metadata.markDeleted();
        repository.save(metadata);
    }

    private void validateOwnerId(String ownerId) {
        if (!StringUtils.hasText(ownerId) || ownerId.length() > 128) {
            throw new InvalidFileException("ownerId is required and must be at most 128 characters");
        }
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String sha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream inputStream = new DigestInputStream(file.getInputStream(), digest)) {
                inputStream.transferTo(java.io.OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception exception) {
            throw new InvalidFileException("Unable to calculate file checksum");
        }
    }

    private void cleanupUploadedObject(String bucket, String objectKey) {
        try {
            objectStorageService.remove(bucket, objectKey);
        } catch (RuntimeException cleanupException) {
            log.warn("Rollback cleanup failed for uploaded file object");
        }
    }

    private QueryMode queryMode(FileCategory category, boolean includeDeleted) {
        if (category != null && includeDeleted) {
            return QueryMode.ALL_WITH_CATEGORY;
        }
        if (category != null) {
            return QueryMode.AVAILABLE_WITH_CATEGORY;
        }
        return includeDeleted ? QueryMode.ALL : QueryMode.AVAILABLE;
    }

    private enum QueryMode {
        ALL,
        AVAILABLE,
        ALL_WITH_CATEGORY,
        AVAILABLE_WITH_CATEGORY
    }
}
