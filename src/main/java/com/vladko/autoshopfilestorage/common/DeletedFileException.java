package com.vladko.autoshopfilestorage.common;

import java.util.UUID;

public class DeletedFileException extends RuntimeException {

    public DeletedFileException(UUID fileId) {
        super("File is deleted: " + fileId);
    }
}
