package com.vladko.autoshopfilestorage.bucket;

public enum FileCategory {
    ORDER_DOCUMENT("documents", CategoryGroup.DOCUMENT),
    ORDER_ESTIMATE("estimates", CategoryGroup.DOCUMENT),
    ORDER_INSPECTION_PHOTO("car-inspections", CategoryGroup.PHOTO),
    VEHICLE_PHOTO("car-inspections", CategoryGroup.PHOTO),
    VEHICLE_DOCUMENT("documents", CategoryGroup.DOCUMENT),
    CUSTOMER_DOCUMENT("documents", CategoryGroup.DOCUMENT),
    CUSTOMER_AVATAR("avatars", CategoryGroup.AVATAR),
    EMPLOYEE_AVATAR("avatars", CategoryGroup.AVATAR),
    INVOICE("estimates", CategoryGroup.DOCUMENT),
    REPORT("estimates", CategoryGroup.REPORT);

    public enum CategoryGroup {
        AVATAR,
        PHOTO,
        DOCUMENT,
        REPORT
    }

    private final String bucketName;
    private final CategoryGroup group;

    FileCategory(String bucketName, CategoryGroup group) {
        this.bucketName = bucketName;
        this.group = group;
    }

    public String bucketName() {
        return bucketName;
    }

    public CategoryGroup group() {
        return group;
    }
}
