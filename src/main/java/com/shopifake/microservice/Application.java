package com.shopifake.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.shopifake.microservice.annotation.Generated;

/**
 * Main application class for the Spring Boot template.
 */
@SpringBootApplication
public class Application {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    @Generated
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

