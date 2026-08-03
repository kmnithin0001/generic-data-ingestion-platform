package com.intentwise.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entrance configuration class for the Generic Data Ingestion Platform.
 * Enables JPA auditing listener support for tracking timestamps automatically.
 */
@SpringBootApplication
public class GenericDataIngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenericDataIngestionApplication.class, args);
    }
}
