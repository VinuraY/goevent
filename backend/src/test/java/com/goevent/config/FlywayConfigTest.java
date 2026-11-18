package com.goevent.config;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Map;
import java.util.Properties;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FlywayConfigTest {
    @Test void forwardsMigrationTrustPropertiesWithoutChangingCredentialsOrUrl() throws Exception {
        var driver = mock(Driver.class);
        when(driver.connect(anyString(), any(Properties.class))).thenReturn(mock(Connection.class));
        var dataSource = new SimpleDriverDataSource(driver, "jdbc:mysql://mysql/goevent_db",
                "goevent_migration", "fixture-migration-password");
        var original = new Properties();
        original.setProperty("socketTimeout", "2000");
        dataSource.setConnectionProperties(original);
        var configuration = Flyway.configure().dataSource(dataSource)
                .jdbcProperties(Map.of("trustCertificateKeyStorePassword", "fixture-truststore-password"));

        new FlywayConfig().migrationConnectionProperties().customize(configuration);
        dataSource.getConnection().close();

        var properties = ArgumentCaptor.forClass(Properties.class);
        verify(driver).connect(eq("jdbc:mysql://mysql/goevent_db"), properties.capture());
        assertEquals("fixture-truststore-password", properties.getValue().getProperty("trustCertificateKeyStorePassword"));
        assertEquals("goevent_migration", properties.getValue().getProperty("user"));
        assertEquals("fixture-migration-password", properties.getValue().getProperty("password"));
        assertEquals("2000", properties.getValue().getProperty("socketTimeout"));
        assertSame(dataSource, configuration.getDataSource());
        assertNull(original.getProperty("trustCertificateKeyStorePassword"));
    }
}
