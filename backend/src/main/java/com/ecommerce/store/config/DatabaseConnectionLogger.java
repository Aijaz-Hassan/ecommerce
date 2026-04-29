package com.ecommerce.store.config;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionLogger {

    private final DataSource dataSource;

    public DatabaseConnectionLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void logDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("========================================");
            System.out.println("Connected to database successfully");
            System.out.println("Database Product: " + metaData.getDatabaseProductName());
            System.out.println("Database URL: " + metaData.getURL());
            System.out.println("Database User: " + metaData.getUserName());
            System.out.println("========================================");
        } catch (Exception exception) {
            System.out.println("Database connection check failed: " + exception.getMessage());
        }
    }
}
