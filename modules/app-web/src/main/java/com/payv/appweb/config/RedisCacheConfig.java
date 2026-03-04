package com.payv.appweb.config;

import com.payv.common.cache.CacheNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;

    @Value("${redis.password:}")
    private String redisPassword;

    @Value("${redis.database:0}")
    private int redisDatabase;

    @Value("${redis.command-timeout-ms:2000}")
    private long redisCommandTimeoutMs;

    @Value("${cache.ttl.default-seconds:300}")
    private long defaultTtlSeconds;

    @Value("${cache.ttl.reporting-monthly-seconds:600}")
    private long reportingMonthlyTtlSeconds;

    @Value("${cache.ttl.reporting-home-seconds:180}")
    private long reportingHomeTtlSeconds;

    @Value("${cache.ttl.ledger-recent-seconds:120}")
    private long ledgerRecentTtlSeconds;

    @Value("${cache.ttl.budget-monthly-seconds:300}")
    private long budgetMonthlyTtlSeconds;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(redisHost);
        standalone.setPort(redisPort);
        standalone.setDatabase(redisDatabase);
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            standalone.setPassword(RedisPassword.of(redisPassword));
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(Math.max(redisCommandTimeoutMs, 100L)))
                .build();

        return new LettuceConnectionFactory(standalone, clientConfig);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();
        RedisSerializationContext.SerializationPair<Object> valuePair =
                RedisSerializationContext.SerializationPair.fromSerializer(serializer);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(valuePair)
                .entryTtl(Duration.ofSeconds(Math.max(defaultTtlSeconds, 1L)));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(
                CacheNames.REPORTING_MONTHLY_SUMMARY,
                defaultConfig.entryTtl(Duration.ofSeconds(Math.max(reportingMonthlyTtlSeconds, 1L)))
        );
        cacheConfigs.put(
                CacheNames.REPORTING_HOME_DASHBOARD,
                defaultConfig.entryTtl(Duration.ofSeconds(Math.max(reportingHomeTtlSeconds, 1L)))
        );
        cacheConfigs.put(
                CacheNames.LEDGER_RECENT_FIRST_PAGE,
                defaultConfig.entryTtl(Duration.ofSeconds(Math.max(ledgerRecentTtlSeconds, 1L)))
        );
        cacheConfigs.put(
                CacheNames.BUDGET_MONTHLY_STATUS,
                defaultConfig.entryTtl(Duration.ofSeconds(Math.max(budgetMonthlyTtlSeconds, 1L)))
        );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis cache GET error (cache={}, key={})", cacheName(cache), key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis cache PUT error (cache={}, key={})", cacheName(cache), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis cache EVICT error (cache={}, key={})", cacheName(cache), key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis cache CLEAR error (cache={})", cacheName(cache), exception);
            }

            private String cacheName(Cache cache) {
                return cache == null ? "unknown" : cache.getName();
            }
        };
    }

}
