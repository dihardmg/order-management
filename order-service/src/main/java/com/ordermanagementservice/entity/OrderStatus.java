package com.ordermanagementservice.entity;

/**
 * Order Status Enumeration
 * Defines the possible states of an order in the system
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
