package com.ordermanagementservice.repository;

import com.ordermanagementservice.dto.CustomerRunningTotalDTO;
import com.ordermanagementservice.dto.SalesReportDTO;
import com.ordermanagementservice.entity.Order;
import com.ordermanagementservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for Order entity
 * Provides database access methods for Order operations
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific customer
     * @param customerId the customer ID
     * @return list of orders
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find orders by date range
     * @param startDate start date
     * @param endDate end date
     * @return list of orders within date range
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByOrderDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find orders by status
     * @param status the order status
     * @return list of orders with given status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Count orders by customer ID
     * @param customerId the customer ID
     * @return number of orders
     */
    long countByCustomerId(Long customerId);

    // ============================================================
    // ADVANCED NATIVE SQL QUERIES (PostgreSQL 17 Features)
    // ============================================================

    /**
     * Generate monthly sales report with customer statistics
     * Uses CTE (Common Table Expression) for complex reporting
     *
     * @param startDate start date for report
     * @param endDate end date for report
     * @return list of monthly sales data with customer stats
     */
    @Query(value = """
        SELECT
            DATE(o.order_date) as report_date,
            COUNT(*) as order_count,
            SUM(o.total_amount) as revenue,
            COUNT(DISTINCT o.customer_id) as unique_customers,
            CASE
                WHEN COUNT(*) > 0 THEN SUM(o.total_amount) / COUNT(*)
                ELSE 0
            END as avg_order_value
        FROM orders o
        WHERE o.order_date BETWEEN :startDate AND :endDate
        GROUP BY DATE(o.order_date)
        ORDER BY report_date
    """, nativeQuery = true)
    List<Object[]> generateDailySalesReport(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get customer running totals using Window Functions
     * Demonstrates PostgreSQL 17 window functions with running totals
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of customers with running totals
     */
    @Query(value = """
        SELECT
            o.customer_id,
            o.order_date,
            o.total_amount,
            SUM(o.total_amount) OVER (
                PARTITION BY o.customer_id
                ORDER BY o.order_date
                ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
            ) as running_total
        FROM orders o
        WHERE o.order_date BETWEEN :startDate AND :endDate
        ORDER BY o.customer_id, o.order_date
    """, nativeQuery = true)
    List<Object[]> getCustomerRunningTotals(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find orders by customer with total amount analysis
     * Uses subquery and aggregation
     *
     * @param customerId customer ID
     * @param minAmount minimum order amount
     * @return list of orders meeting criteria
     */
    @Query(value = """
        SELECT *
        FROM orders
        WHERE customer_id = :customerId
            AND total_amount > :minAmount
        ORDER BY order_date DESC, total_amount DESC
    """, nativeQuery = true)
    List<Order> findHighValueOrdersByCustomer(
        @Param("customerId") Long customerId,
        @Param("minAmount") Double minAmount
    );

    /**
     * Get top customers by total spending
     * Uses GROUP BY, HAVING, and ORDER BY
     *
     * @param limit number of top customers to return
     * @return list of customer IDs with total spending
     */
    @Query(value = """
        SELECT
            customer_id,
            COUNT(*) as order_count,
            SUM(total_amount) as total_spent
        FROM orders
        GROUP BY customer_id
        HAVING SUM(total_amount) > 0
        ORDER BY total_spent DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopCustomers(@Param("limit") Integer limit);

    /**
     * Find orders with specific status in date range
     * Demonstrates date range filtering with status
     *
     * @param statuses list of order statuses
     * @param startDate start date
     * @param endDate end date
     * @return list of orders
     */
    @Query(value = """
        SELECT *
        FROM orders
        WHERE status IN :statuses
            AND order_date BETWEEN :startDate AND :endDate
        ORDER BY order_date DESC
    """, nativeQuery = true)
    List<Order> findByStatusInAndDateBetween(
        @Param("statuses") List<String> statuses,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get order summaries with item counts for a specific date
     * Uses LEFT JOIN to count items efficiently
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of order IDs with item counts
     */
    @Query(value = """
        SELECT
            o.id as orderId,
            o.customer_id as customerId,
            o.order_date as orderDate,
            o.status,
            o.total_amount as totalAmount,
            COUNT(oi.id) as itemCount
        FROM orders o
        LEFT JOIN order_items oi ON o.id = oi.order_id
        WHERE o.order_date BETWEEN :startDate AND :endDate
            AND o.total_amount > 100
        GROUP BY o.id, o.customer_id, o.order_date, o.status, o.total_amount
        ORDER BY o.total_amount DESC
        LIMIT 100
    """, nativeQuery = true)
    List<Object[]> findOrderSummariesWithItemCounts(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
