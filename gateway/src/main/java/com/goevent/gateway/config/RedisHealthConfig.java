package com.goevent.gateway.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
public class RedisHealthConfig {
    @Bean
    ReactiveHealthIndicator redisHealthContributor(ReactiveRedisConnectionFactory connections) {
        // The restricted gateway identity can PING; it deliberately cannot run INFO.
        return () -> Mono.usingWhen(Mono.fromSupplier(connections::getReactiveConnection),
                        ReactiveRedisConnection::ping, ReactiveRedisConnection::closeLater)
                .map(reply -> "PONG".equals(reply) ? Health.up().build() : Health.down().build())
                .defaultIfEmpty(Health.down().build())
                .onErrorReturn(Health.down().build());
    }
}
