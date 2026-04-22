package com.vladko.autoshopfilestorage.config;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageProperties {

    @Valid
    private final Minio minio = new Minio();

    @Valid
    private final Presign presign = new Presign();

    @Valid
    private final Validation validation = new Validation();

    public Minio getMinio() {
        return minio;
    }

    public Presign getPresign() {
        return presign;
    }

    public Validation getValidation() {
        return validation;
    }

    public static class Minio {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private boolean initializeBuckets = true;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public boolean isInitializeBuckets() {
            return initializeBuckets;
        }

        public void setInitializeBuckets(boolean initializeBuckets) {
            this.initializeBuckets = initializeBuckets;
        }
    }

    public static class Presign {
        @Min(60)
        @Max(3600)
        private int defaultExpirySeconds = 900;

        @Min(60)
        @Max(86400)
        private int maxExpirySeconds = 3600;

        public int getDefaultExpirySeconds() {
            return defaultExpirySeconds;
        }

        public void setDefaultExpirySeconds(int defaultExpirySeconds) {
            this.defaultExpirySeconds = defaultExpirySeconds;
        }

        public int getMaxExpirySeconds() {
            return maxExpirySeconds;
        }

        public void setMaxExpirySeconds(int maxExpirySeconds) {
            this.maxExpirySeconds = maxExpirySeconds;
        }
    }

    public static class Validation {
        @Valid
        private Map<FileCategory, CategoryRule> categories = defaultRules();

        public Map<FileCategory, CategoryRule> getCategories() {
            return categories;
        }

        public void setCategories(Map<FileCategory, CategoryRule> categories) {
            EnumMap<FileCategory, CategoryRule> target = new EnumMap<>(FileCategory.class);
            if (categories != null) {
                target.putAll(categories);
            }
            this.categories = target;
        }

        public CategoryRule ruleFor(FileCategory category) {
            return categories.get(category);
        }

        private static Map<FileCategory, CategoryRule> defaultRules() {
            EnumMap<FileCategory, CategoryRule> rules = new EnumMap<>(FileCategory.class);
            CategoryRule avatar = new CategoryRule(
                    DataSize.ofMegabytes(5),
                    List.of("image/jpeg", "image/png", "image/webp"),
                    List.of("jpg", "jpeg", "png", "webp")
            );
            CategoryRule photo = new CategoryRule(
                    DataSize.ofMegabytes(15),
                    List.of("image/jpeg", "image/png", "image/webp"),
                    List.of("jpg", "jpeg", "png", "webp")
            );
            CategoryRule document = new CategoryRule(
                    DataSize.ofMegabytes(20),
                    List.of("application/pdf", "image/jpeg", "image/png", "text/plain"),
                    List.of("pdf", "jpg", "jpeg", "png", "txt")
            );
            CategoryRule report = new CategoryRule(
                    DataSize.ofMegabytes(20),
                    List.of(
                            "application/pdf",
                            "text/csv",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    ),
                    List.of("pdf", "csv", "xlsx")
            );
            for (FileCategory category : FileCategory.values()) {
                rules.put(category, switch (category.group()) {
                    case AVATAR -> avatar;
                    case PHOTO -> photo;
                    case DOCUMENT -> document;
                    case REPORT -> report;
                });
            }
            return rules;
        }
    }

    public static class CategoryRule {
        @DataSizeUnit(DataUnit.BYTES)
        private DataSize maxSize;
        private List<String> contentTypes;
        private List<String> extensions;

        public CategoryRule() {
        }

        public CategoryRule(DataSize maxSize, List<String> contentTypes, List<String> extensions) {
            this.maxSize = maxSize;
            this.contentTypes = contentTypes;
            this.extensions = extensions;
        }

        public DataSize getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(DataSize maxSize) {
            this.maxSize = maxSize;
        }

        public List<String> getContentTypes() {
            return contentTypes;
        }

        public void setContentTypes(List<String> contentTypes) {
            this.contentTypes = contentTypes;
        }

        public List<String> getExtensions() {
            return extensions;
        }

        public void setExtensions(List<String> extensions) {
            this.extensions = extensions;
        }
    }
}
