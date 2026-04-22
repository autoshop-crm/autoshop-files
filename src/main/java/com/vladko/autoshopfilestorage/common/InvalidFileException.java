package com.vladko.autoshopfilestorage.common;

public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
