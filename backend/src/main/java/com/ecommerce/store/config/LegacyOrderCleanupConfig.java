package com.ecommerce.store.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class LegacyOrderCleanupConfig {

    @Bean
    ApplicationRunner legacyOrderCleanupRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("DROP TABLE IF EXISTS order_items");
            jdbcTemplate.execute("DROP TABLE IF EXISTS orders");
        };
    }
}
