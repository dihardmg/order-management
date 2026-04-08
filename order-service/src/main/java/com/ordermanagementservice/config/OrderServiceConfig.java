package com.ordermanagementservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class for Order Service
 * Enables JPA auditing and additional configurations
 */
@Configuration
@EnableJpaAuditing
public class OrderServiceConfig {
    // Additional configuration can be added here
}
