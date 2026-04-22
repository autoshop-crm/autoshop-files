package com.vladko.autoshopfilestorage.validation;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Component
public class ObjectKeyFactory {

    private final Clock clock;

    public ObjectKeyFactory(Clock clock) {
        this.clock = clock;
    }

    public String create(FileCategory category, UUID fileId, String sanitizedFilename, String checksumSha256) {
        LocalDate today = LocalDate.now(clock);
        String extension = extension(sanitizedFilename);
        String baseName = baseName(sanitizedFilename);
        String suffix = checksumSha256 == null || checksumSha256.length() < 8
                ? fileId.toString().substring(0, 8)
                : checksumSha256.substring(0, 8);
        return "%s/%04d/%02d/%02d/%s/%s-%s.%s".formatted(
                category.name().toLowerCase(Locale.ROOT).replace('_', '-'),
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                fileId,
                baseName,
                suffix,
                extension
        );
    }

    private String extension(String sanitizedFilename) {
        int dotIndex = sanitizedFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == sanitizedFilename.length() - 1) {
            return "bin";
        }
        return sanitizedFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String baseName(String sanitizedFilename) {
        int dotIndex = sanitizedFilename.lastIndexOf('.');
        String base = dotIndex > 0 ? sanitizedFilename.substring(0, dotIndex) : sanitizedFilename;
        return base.isBlank() ? "file" : base;
    }
}
