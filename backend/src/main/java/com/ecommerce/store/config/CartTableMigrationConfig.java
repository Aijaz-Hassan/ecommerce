package com.ecommerce.store.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CartTableMigrationConfig {

    @Bean
    ApplicationRunner cartTableMigrationRunner(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                boolean cartExists = tableExists(metaData, "cart");
                boolean cartsExists = tableExists(metaData, "carts");

                if (!cartExists && cartsExists) {
                    jdbcTemplate.execute("ALTER TABLE carts RENAME TO cart");
                }
            }
        };
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws Exception {
        try (ResultSet tables = metaData.getTables(null, null, tableName, null)) {
            if (tables.next()) {
                return true;
            }
        }

        try (ResultSet tables = metaData.getTables(null, null, tableName.toUpperCase(), null)) {
            return tables.next();
        }
    }
}
