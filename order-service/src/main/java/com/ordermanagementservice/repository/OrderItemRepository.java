package com.ordermanagementservice.repository;

import com.ordermanagementservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for OrderItem entity
 * Provides database access methods for OrderItem operations
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items for a specific order
     * @param orderId the order ID
     * @return list of order items
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find order items by product ID
     * @param productId the product ID
     * @return list of order items
     */
    List<OrderItem> findByProductId(Long productId);
}
