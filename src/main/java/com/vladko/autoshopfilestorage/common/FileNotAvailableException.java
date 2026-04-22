package com.vladko.autoshopfilestorage.common;

import java.util.UUID;

public class FileNotAvailableException extends RuntimeException {

    public FileNotAvailableException(UUID fileId, String status) {
        super("File is not available for download: " + fileId + " status=" + status);
    }
}
