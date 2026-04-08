package com.ordermanagementservice.exception;

/**
 * Exception thrown when product stock is insufficient for order
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(Long productId, Integer requested, Integer available) {
        super(String.format("Insufficient stock for product ID %d. Requested: %d, Available: %d",
            productId, requested, available));
    }
}
