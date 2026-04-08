package com.ordermanagementservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Cache Configuration for Order Service
 * Enables caching and configures cache manager with in-memory caches
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager with in-memory concurrent map caches
     * Suitable for development and small to medium applications
     *
     * @return CacheManager with configured caches
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("orders"),
                new ConcurrentMapCache("products"),
                new ConcurrentMapCache("customers"),
                new ConcurrentMapCache("categories"),
                new ConcurrentMapCache("analytics"),
                new ConcurrentMapCache("reports")
        ));

        return cacheManager;
    }
}
