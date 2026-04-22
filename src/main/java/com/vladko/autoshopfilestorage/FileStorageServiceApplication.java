package com.vladko.autoshopfilestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FileStorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileStorageServiceApplication.class, args);
    }

}
