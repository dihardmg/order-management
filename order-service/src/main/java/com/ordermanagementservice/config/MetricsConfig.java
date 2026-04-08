package com.ordermanagementservice.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics Configuration for Order Service
 * Configures Micrometer for performance monitoring and metrics collection
 */
@Configuration
public class MetricsConfig {

    /**
     * Configure common tags for all metrics
     * Adds application-level metadata to metrics
     *
     * @param registry MeterRegistry
     * @return MeterRegistryCustomizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "order-service",
                        "region", "production",
                        "environment", "docker"
                );
    }

    /**
     * Custom Timer for measuring order creation time
     * Demonstrates manual timing with Micrometer
     *
     * @param registry MeterRegistry
     * @return Timer instance
     */
    @Bean
    public Timer orderCreationTimer(MeterRegistry registry) {
        return Timer.builder("order.creation.duration")
                .description("Time taken to create orders")
                .tags("operation", "create")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    /**
     * Custom Timer for measuring query execution time
     *
     * @param registry MeterRegistry
     * @return Timer instance
     */
    @Bean
    public Timer queryExecutionTimer(MeterRegistry registry) {
        return Timer.builder("database.query.duration")
                .description("Time taken to execute database queries")
                .tags("operation", "query")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    /**
     * Inner interface for MeterRegistryCustomizer
     */
    @FunctionalInterface
    public interface MeterRegistryCustomizer<T extends MeterRegistry> {
        void customize(T registry);
    }
}
