package com.shopifake.microservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the application.
 * Allows cross-origin requests based on the active profile.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Maximum age for CORS preflight cache in seconds.
     */
    private static final long CORS_MAX_AGE = 3600;

    /**
     * Allowed origins for CORS.
     */
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    /**
     * Allowed HTTP methods for CORS.
     */
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;

    /**
     * Allowed headers for CORS.
     */
    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    /**
     * Whether credentials are allowed in CORS requests.
     */
    @Value("${cors.allow-credentials:false}")
    private boolean allowCredentials;

    /**
     * Configure CORS mappings.
     *
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(CORS_MAX_AGE);
    }
}

