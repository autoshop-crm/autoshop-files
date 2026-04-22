package com.vladko.autoshopfilestorage.validation;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.common.InvalidFileException;
import com.vladko.autoshopfilestorage.config.FileStorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileUploadValidatorTest {

    private final FileUploadValidator validator = new FileUploadValidator(
            new FileStorageProperties(),
            new FilenameSanitizer()
    );

    @Test
    void acceptsAllowedAvatarUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile photo.PNG",
                "image/png",
                "image-bytes".getBytes()
        );

        ValidatedUpload upload = validator.validate(FileCategory.CUSTOMER_AVATAR, file);

        assertThat(upload.sanitizedFilename()).isEqualTo("profile_photo.PNG");
        assertThat(upload.extension()).isEqualTo("png");
        assertThat(upload.contentType()).isEqualTo("image/png");
    }

    @Test
    void rejectsPathTraversalFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../secret.pdf",
                "application/pdf",
                "bytes".getBytes()
        );

        assertThatThrownBy(() -> validator.validate(FileCategory.ORDER_DOCUMENT, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("unsafe path");
    }

    @Test
    void rejectsMismatchedContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "application/pdf",
                "bytes".getBytes()
        );

        assertThatThrownBy(() -> validator.validate(FileCategory.CUSTOMER_AVATAR, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Content type");
    }
}
