package it.gov.pagopa.merchant.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;

import static it.gov.pagopa.merchant.constants.PdndConst.*;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfiguration {

    @Bean(REDIS_CACHE_MANAGER)
    public CacheManager redisCacheManager(
            ObjectProvider<RedisConnectionFactory> connectionFactoryProvider,
            @Value("${spring.redis.enabled:true}") boolean redisCacheEnabled,
            @Value("${cache.pdnd-token-ttl-seconds:60}") long tokenTtlSeconds,
            @Value("${cache.pdnd-client-assertion-ttl-seconds:3300}") long assertionTtlSeconds,
            @Value("${cache.pdnd-ateco-codes-ttl-seconds:28800}") long atecoCodesTtlSeconds
    ) {
        if (!redisCacheEnabled) {
            log.warn("[CACHE] Redis cache disabled (spring.redis.enabled=false). Using NoOpCacheManager.");
            return new NoOpCacheManager();
        }

        RedisConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
        if (connectionFactory == null) {
            throw new IllegalStateException("RedisConnectionFactory not available while spring.redis.enabled=true");
        }

        log.info("[CACHE] Redis cache enabled. TTLs -> token: {}s, assertion: {}s, atecoCodes: {}s",
                tokenTtlSeconds, assertionTtlSeconds, atecoCodesTtlSeconds);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                PDND_TOKEN_CACHE,
                defaultConfig.entryTtl(Duration.ofSeconds(tokenTtlSeconds)),

                PDND_VISURA_TOKEN_CACHE,
                defaultConfig.entryTtl(Duration.ofSeconds(tokenTtlSeconds)),

                PDND_CLIENT_ASSERTION_CACHE,
                defaultConfig.entryTtl(Duration.ofSeconds(assertionTtlSeconds)),

                PDND_VISURA_CLIENT_ASSERTION_CACHE,
                defaultConfig.entryTtl(Duration.ofSeconds(assertionTtlSeconds)),

                PDND_ATECO_CODES,
                defaultConfig.entryTtl(Duration.ofSeconds(atecoCodesTtlSeconds))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

