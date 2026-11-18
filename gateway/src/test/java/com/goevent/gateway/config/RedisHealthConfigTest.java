package com.goevent.gateway.config;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisHealthConfigTest {
    @Test void usesOnlyPingAndReleasesTheConnection() {
        var connections = mock(ReactiveRedisConnectionFactory.class);
        var connection = mock(ReactiveRedisConnection.class);
        when(connections.getReactiveConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn(Mono.just("PONG"));
        when(connection.closeLater()).thenReturn(Mono.empty());

        var health = new RedisHealthConfig().redisHealthContributor(connections)
                .health().block(Duration.ofSeconds(2));

        assertNotNull(health);
        assertEquals("UP", health.getStatus().getCode());
        verify(connection).ping();
        verify(connection).closeLater();
        verifyNoMoreInteractions(connection);
    }

    @Test void failsClosedForUnexpectedEmptyOrFailedReplies() {
        for (Mono<String> reply : List.of(Mono.just("unexpected"), Mono.<String>empty(),
                Mono.<String>error(new IllegalStateException("Redis unavailable")))) {
            var connections = mock(ReactiveRedisConnectionFactory.class);
            var connection = mock(ReactiveRedisConnection.class);
            when(connections.getReactiveConnection()).thenReturn(connection);
            when(connection.ping()).thenReturn(reply);
            when(connection.closeLater()).thenReturn(Mono.empty());

            var health = new RedisHealthConfig().redisHealthContributor(connections)
                    .health().block(Duration.ofSeconds(2));

            assertNotNull(health);
            assertEquals("DOWN", health.getStatus().getCode());
            verify(connection).closeLater();
        }
    }
}
