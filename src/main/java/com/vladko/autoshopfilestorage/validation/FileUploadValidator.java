package com.vladko.autoshopfilestorage.validation;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.common.InvalidFileException;
import com.vladko.autoshopfilestorage.config.FileStorageProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Component
public class FileUploadValidator {

    private final FileStorageProperties properties;
    private final FilenameSanitizer filenameSanitizer;

    public FileUploadValidator(FileStorageProperties properties, FilenameSanitizer filenameSanitizer) {
        this.properties = properties;
        this.filenameSanitizer = filenameSanitizer;
    }

    public ValidatedUpload validate(FileCategory category, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty");
        }

        FileStorageProperties.CategoryRule rule = properties.getValidation().ruleFor(category);
        if (rule == null) {
            throw new InvalidFileException("Unsupported file category");
        }

        if (file.getSize() > rule.getMaxSize().toBytes()) {
            throw new InvalidFileException("File size exceeds limit for category " + category);
        }

        String sanitizedFilename = filenameSanitizer.sanitize(file.getOriginalFilename());
        String extension = filenameSanitizer.extension(sanitizedFilename);
        if (!rule.getExtensions().contains(extension)) {
            throw new InvalidFileException("File extension is not allowed for category " + category);
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!rule.getContentTypes().contains(contentType)) {
            throw new InvalidFileException("Content type is not allowed for category " + category);
        }

        return new ValidatedUpload(sanitizedFilename, extension, contentType, file.getSize());
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            throw new InvalidFileException("Content type is required");
        }
        return contentType.toLowerCase(Locale.ROOT);
    }
}
