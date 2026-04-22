package com.vladko.autoshopfilestorage.storage;

import java.io.InputStream;

public record StoredObject(InputStream inputStream, String contentType, long size) {
}
