package com.vladko.autoshopfilestorage.file;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.file.dto.FileMetadataResponse;
import com.vladko.autoshopfilestorage.file.dto.OwnerFilesResponse;
import com.vladko.autoshopfilestorage.file.dto.PresignedUrlRequest;
import com.vladko.autoshopfilestorage.file.dto.PresignedUrlResponse;
import com.vladko.autoshopfilestorage.storage.StoredObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<FileMetadataResponse> upload(
            @RequestParam FileCategory category,
            @RequestParam OwnerType ownerType,
            @RequestParam @NotBlank String ownerId,
            @RequestParam(required = false) String uploadedBy,
            @RequestPart MultipartFile file
    ) {
        FileMetadataResponse response = fileService.upload(category, ownerType, ownerId, uploadedBy, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fileId}")
    FileMetadataResponse getMetadata(@PathVariable UUID fileId) {
        return fileService.getMetadata(fileId);
    }

    @GetMapping
    OwnerFilesResponse listByOwner(
            @RequestParam OwnerType ownerType,
            @RequestParam @NotBlank String ownerId,
            @RequestParam(required = false) FileCategory category,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return fileService.listByOwner(ownerType, ownerId, category, includeDeleted, page, size);
    }

    @GetMapping("/{fileId}/download")
    ResponseEntity<InputStreamResource> download(@PathVariable UUID fileId) {
        FileMetadataResponse metadata = fileService.getMetadata(fileId);
        StoredObject storedObject = fileService.download(fileId);
        return ResponseEntity.ok()
                .contentType(parseMediaType(storedObject.contentType()))
                .contentLength(storedObject.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(metadata.originalFilename())
                        .build()
                        .toString())
                .body(new InputStreamResource(storedObject.inputStream()));
    }

    @PostMapping("/{fileId}/presigned-download-url")
    PresignedUrlResponse presignedUrl(
            @PathVariable UUID fileId,
            @RequestBody(required = false) @Valid PresignedUrlRequest request
    ) {
        Integer ttlSeconds = request == null ? null : request.ttlSeconds();
        return fileService.presignedDownloadUrl(fileId, ttlSeconds);
    }

    @DeleteMapping("/{fileId}")
    ResponseEntity<Void> delete(@PathVariable UUID fileId) {
        fileService.delete(fileId);
        return ResponseEntity.noContent().build();
    }

    private MediaType parseMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
