package com.vladko.autoshopfilestorage.bucket;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileCategoryTest {

    @Test
    void mapsCategoriesToFixedBuckets() {
        assertThat(FileCategory.ORDER_INSPECTION_PHOTO.bucketName()).isEqualTo("car-inspections");
        assertThat(FileCategory.ORDER_DOCUMENT.bucketName()).isEqualTo("documents");
        assertThat(FileCategory.CUSTOMER_AVATAR.bucketName()).isEqualTo("avatars");
        assertThat(FileCategory.ORDER_ESTIMATE.bucketName()).isEqualTo("estimates");
    }
}
