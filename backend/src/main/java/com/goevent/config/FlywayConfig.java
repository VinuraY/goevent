package com.goevent.config;

import java.util.Properties;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration(proxyBeanMethods = false)
public class FlywayConfig {
    @Bean
    FlywayConfigurationCustomizer migrationConnectionProperties() {
        return configuration -> {
            // Boot creates this separate datasource before applying Flyway JDBC properties.
            if (!configuration.getJdbcProperties().isEmpty()
                    && configuration.getDataSource() instanceof SimpleDriverDataSource dataSource) {
                var properties = new Properties();
                if (dataSource.getConnectionProperties() != null) {
                    properties.putAll(dataSource.getConnectionProperties());
                }
                properties.putAll(configuration.getJdbcProperties());
                dataSource.setConnectionProperties(properties);
            }
        };
    }
}
