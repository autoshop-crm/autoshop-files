package com.vladko.autoshopfilestorage.validation;

import com.vladko.autoshopfilestorage.common.InvalidFileException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class FilenameSanitizer {

    private static final int MAX_FILENAME_LENGTH = 120;

    public String sanitize(String filename) {
        String candidate = StringUtils.cleanPath(filename == null ? "" : filename).replace('\\', '/');
        if (!StringUtils.hasText(candidate)) {
            throw new InvalidFileException("Original filename is required");
        }
        if (candidate.contains("..") || candidate.contains("/") || candidate.contains("\u0000")) {
            throw new InvalidFileException("Filename contains unsafe path segments");
        }
        String normalized = Normalizer.normalize(candidate, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^[._-]+", "")
                .replaceAll("[._-]+$", "");
        if (!StringUtils.hasText(normalized)) {
            throw new InvalidFileException("Filename contains no safe characters");
        }
        if (normalized.length() > MAX_FILENAME_LENGTH) {
            normalized = shorten(normalized);
        }
        return normalized;
    }

    public String extension(String sanitizedFilename) {
        int dotIndex = sanitizedFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == sanitizedFilename.length() - 1) {
            return "";
        }
        return sanitizedFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String shorten(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            String extension = filename.substring(dotIndex);
            int nameLimit = Math.max(1, MAX_FILENAME_LENGTH - extension.length());
            return filename.substring(0, Math.min(nameLimit, dotIndex)) + extension;
        }
        return filename.substring(0, MAX_FILENAME_LENGTH);
    }
}
