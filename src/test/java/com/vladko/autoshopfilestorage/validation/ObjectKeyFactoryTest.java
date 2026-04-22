package com.vladko.autoshopfilestorage.validation;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.file.OwnerType;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectKeyFactoryTest {

    private final ObjectKeyFactory factory = new ObjectKeyFactory(
            Clock.fixed(Instant.parse("2026-04-20T10:15:30Z"), ZoneOffset.UTC)
    );

    @Test
    void createsServerControlledObjectKey() {
        UUID fileId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        String key = factory.create(FileCategory.ORDER_INSPECTION_PHOTO, fileId, "photo.jpg", "abcdef1234567890");

        assertThat(key).isEqualTo("order-inspection-photo/2026/04/20/11111111-1111-1111-1111-111111111111/photo-abcdef12.jpg");
        assertThat(key).doesNotContain("..");
        assertThat(key).doesNotContain("order/42");
    }
}
