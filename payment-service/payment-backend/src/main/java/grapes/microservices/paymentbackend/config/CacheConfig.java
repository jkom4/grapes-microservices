package grapes.microservices.paymentbackend.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

/* * Cache configuration for Redis.
 * This configuration sets up a default cache and a specific cache configuration for "pendingPayments".
 * Note: Ensure that the JdkSerializationRedisSerializer is compatible with your data types.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration PENDING_PAYMENTS_CACHE_TTL = Duration.ofMinutes(10);
    public static final String PENDING_PAYMENTS_CACHE_NAME = "pendingPayments";

    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration() {

        ClassLoader classLoader = this.getClass().getClassLoader();
        JdkSerializationRedisSerializer redisSerializer = new JdkSerializationRedisSerializer(classLoader);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_CACHE_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(redisSerializer));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        JdkSerializationRedisSerializer redisSerializer = new JdkSerializationRedisSerializer(classLoader);
        return (builder) -> builder
                .withCacheConfiguration(PENDING_PAYMENTS_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(PENDING_PAYMENTS_CACHE_TTL)
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair.fromSerializer(redisSerializer))
                );
    }
}