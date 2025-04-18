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
 * This configuration sets up a default cache with a 30-minute TTL and a specific cache
 * configuration for "pendingPayments" with a 10-minute TTL.
 *
 * Note: Ensure that the JdkSerializationRedisSerializer is compatible with your data types.
 */
@Configuration
@EnableCaching // Enable caching support
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration() {

        ClassLoader classLoader = this.getClass().getClassLoader();
        JdkSerializationRedisSerializer redisSerializer = new JdkSerializationRedisSerializer(classLoader);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default TTL (can be adjusted)
                .disableCachingNullValues() // Important: don't cache null values
                .serializeValuesWith(SerializationPair.fromSerializer(redisSerializer));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        // Custom configuration for 'pendingPayments' cache
        ClassLoader classLoader = this.getClass().getClassLoader();
        JdkSerializationRedisSerializer redisSerializer = new JdkSerializationRedisSerializer(classLoader);

        return (builder) -> builder
                .withCacheConfiguration("pendingPayments", // Exact cache name to use
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10)) // 10-minute TTL for pending payments
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair.fromSerializer(redisSerializer))
                );
    }
}