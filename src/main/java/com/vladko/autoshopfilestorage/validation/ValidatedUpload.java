package com.vladko.autoshopfilestorage.validation;

public record ValidatedUpload(String sanitizedFilename, String extension, String contentType, long size) {
}
