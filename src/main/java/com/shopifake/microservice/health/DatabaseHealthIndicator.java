package com.shopifake.microservice.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Custom health indicator for database connectivity.
 * Checks if the database connection is available and responsive.
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    /**
     * Timeout for database connection check in seconds.
     */
    private static final int CONNECTION_TIMEOUT = 3;

    /**
     * DataSource to check.
     */
    private final DataSource dataSource;

    /**
     * Constructor.
     *
     * @param dataSource the data source to check
     */
    public DatabaseHealthIndicator(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Check database health.
     *
     * @return health status
     */
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(CONNECTION_TIMEOUT)) {
                return Health.up()
                        .withDetail("database", "Available")
                        .withDetail("validationQuery", "Connection.isValid()")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "Connection validation failed")
                        .build();
            }
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("database", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

