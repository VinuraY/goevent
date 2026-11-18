package com.goevent.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.session.data.redis.autoconfigure.SessionDataRedisProperties;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

class RedisSessionConfigTest {
    @Test void bindsTheAclSessionNamespaceAndDisablesRuntimeConfigCommands() throws Exception {
        var environment = new StandardEnvironment();
        var yaml = new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"));
        environment.getPropertySources().addFirst(yaml.getFirst());
        assertEquals("goevent:session", Binder.get(environment)
                .bind("spring.session.data.redis", Bindable.of(SessionDataRedisProperties.class)).get().getNamespace());

        var production = new Properties();
        var path = Path.of(System.getProperty("basedir")).getParent()
                .resolve("infra/config/backend/application.properties");
        try (var source = Files.newInputStream(path)) {
            production.load(source);
        }
        environment.getPropertySources().addFirst(new PropertiesPropertySource("production", production));
        var settings = Binder.get(environment)
                .bind("spring.session.data.redis", Bindable.of(SessionDataRedisProperties.class)).get();
        assertEquals("goevent:session", settings.getNamespace());
        assertEquals("NONE", settings.getConfigureAction().name());
    }
}
