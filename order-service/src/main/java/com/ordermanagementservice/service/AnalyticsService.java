package com.ordermanagementservice.service;

import com.ordermanagementservice.dto.CustomerRunningTotalDTO;
import com.ordermanagementservice.dto.HighValueCustomerDTO;
import com.ordermanagementservice.dto.OrderSummaryDTO;
import com.ordermanagementservice.dto.SalesReportDTO;
import com.ordermanagementservice.entity.Order;
import com.ordermanagementservice.entity.OrderStatus;
import com.ordermanagementservice.mapper.OrderMapper;
import com.ordermanagementservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    // ============================================================
    // JAVA STREAM PROCESSING EXAMPLES
    // ============================================================

    /**
     * Get order status breakdown using Java Stream groupingBy
     * Demonstrates simple grouping and counting
     *
     * @param startDate start date
     * @param endDate end date
     * @return map of status to count
     */
    public Map<OrderStatus, Long> getOrderStatusBreakdown(LocalDate startDate, LocalDate endDate) {
        log.info("Generating order status breakdown from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * Find high-value customers using Java Stream operations
     * Demonstrates map, filter, sorted, and collect
     * Uses @Cacheable for expensive stream operations
     *
     * @param minPurchase minimum purchase amount
     * @return list of high-value customers sorted by total spent
     */
    @Cacheable(value = "analytics", key = "'high-value-' + #minPurchase")
    public List<HighValueCustomerDTO> findHighValueCustomers(double minPurchase) {
        log.info("Finding high-value customers with minimum purchase: {}", minPurchase);

        List<Order> allOrders = orderRepository.findAll();

        return allOrders.stream()
                .collect(Collectors.groupingBy(Order::getCustomerId))
                .entrySet().stream()
                .map(entry -> {
                    Long customerId = entry.getKey();
                    List<Order> customerOrders = entry.getValue();

                    double totalSpent = customerOrders.stream()
                            .mapToDouble(order -> order.getTotalAmount().doubleValue())
                            .sum();

                    int orderCount = customerOrders.size();
                    double avgOrderValue = totalSpent / orderCount;

                    // Get customer name from first order (simplified approach)
                    String customerName = "Customer " + customerId;
                    String customerEmail = "customer" + customerId + "@email.com";

                    return HighValueCustomerDTO.builder()
                            .customerId(customerId)
                            .customerName(customerName)
                            .customerEmail(customerEmail)
                            .totalSpent(java.math.BigDecimal.valueOf(totalSpent))
                            .orderCount(orderCount)
                            .avgOrderValue(java.math.BigDecimal.valueOf(avgOrderValue))
                            .build();
                })
                .filter(customer -> customer.getTotalSpent().doubleValue() > minPurchase)
                .sorted(Comparator.comparing(HighValueCustomerDTO::getTotalSpent).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Generate order summaries for a specific date
     * Demonstrates map, filter, sorted, limit operations
     *
     * @param date date to generate summaries for
     * @return list of order summaries
     */
    public List<OrderSummaryDTO> generateOrderSummaries(LocalDate date) {
        log.info("Generating order summaries for date: {}", date);

        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(23, 59, 59);

        List<Object[]> results = orderRepository.findOrderSummariesWithItemCounts(startDateTime, endDateTime);

        return results.stream()
                .map(result -> OrderSummaryDTO.builder()
                        .orderId(((Number) result[0]).longValue())
                        .customerId(((Number) result[1]).longValue())
                        .orderDate(((java.time.Instant) result[2]).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                        .status((String) result[3])
                        .totalAmount(java.math.BigDecimal.valueOf(((Number) result[4]).doubleValue()))
                        .itemCount(((Number) result[5]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Calculate daily revenue for a date range
     * Demonstrates grouping by date and summing
     *
     * @param startDate start date
     * @param endDate end date
     * @return map of date to revenue
     */
    public Map<LocalDate, java.math.BigDecimal> calculateDailyRevenue(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating daily revenue from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().toLocalDate(),
                        Collectors.mapping(
                                Order::getTotalAmount,
                                Collectors.reducing(
                                        java.math.BigDecimal.ZERO,
                                        java.math.BigDecimal::add
                                )
                        )
                ));
    }

    /**
     * Process large dataset using parallel stream
     * Demonstrates parallel processing for performance
     *
     * @param orders list of orders to process
     * @return list of processed statistics
     */
    public List<Map<String, Object>> processLargeDatasetParallel(List<Order> orders) {
        log.info("Processing large dataset with {} orders using parallel stream", orders.size());

        return orders.parallelStream()
                .collect(Collectors.groupingByConcurrent(
                        Order::getCustomerId,
                        Collectors.toList()
                ))
                .entrySet().parallelStream()
                .<Map<String, Object>>map(entry -> {
                    Long customerId = entry.getKey();
                    List<Order> customerOrders = entry.getValue();

                    double totalSpent = customerOrders.stream()
                            .mapToDouble(order -> order.getTotalAmount().doubleValue())
                            .sum();

                    Map<String, Object> result = new HashMap<>();
                    result.put("customerId", customerId);
                    result.put("orderCount", customerOrders.size());
                    result.put("totalSpent", totalSpent);
                    result.put("avgOrderValue", totalSpent / customerOrders.size());
                    return result;
                })
                .collect(Collectors.toList());
    }

    // ============================================================
    // REPORTING METHODS USING NATIVE SQL
    // ============================================================

    /**
     * Generate daily sales report (grouped by date)
     * Shows sales metrics for each day in the date range
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of daily sales data
     */
    @Cacheable(value = "reports", key = "'daily-sales-' + #startDate + '-' + #endDate")
    public List<SalesReportDTO> generateMonthlySalesReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating daily sales report from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Object[]> results = orderRepository.generateDailySalesReport(startDateTime, endDateTime);

        return results.stream()
                .map(result -> {
                    // Handle date conversion from various possible types
                    LocalDate reportDate;
                    if (result[0] instanceof java.time.Instant) {
                        reportDate = ((java.time.Instant) result[0]).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    } else if (result[0] instanceof java.sql.Date) {
                        reportDate = ((java.sql.Date) result[0]).toLocalDate();
                    } else if (result[0] instanceof java.time.LocalDateTime) {
                        reportDate = ((java.time.LocalDateTime) result[0]).toLocalDate();
                    } else {
                        reportDate = (LocalDate) result[0];
                    }

                    Long orderCount = ((Number) result[1]).longValue();
                    double revenue = ((Number) result[2]).doubleValue();
                    Long uniqueCustomers = ((Number) result[3]).longValue();
                    double avgOrderValue = ((Number) result[4]).doubleValue();

                    return SalesReportDTO.builder()
                            .reportDate(reportDate)
                            .orderCount(orderCount)
                            .revenue(java.math.BigDecimal.valueOf(revenue))
                            .uniqueCustomers(uniqueCustomers)
                            .avgOrderValue(java.math.BigDecimal.valueOf(avgOrderValue).setScale(2, java.math.RoundingMode.HALF_UP))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get customer running totals using Window Functions
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of customers with running totals
     */
    public List<CustomerRunningTotalDTO> getCustomerRunningTotals(LocalDate startDate, LocalDate endDate) {
        log.info("Getting customer running totals from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Object[]> results = orderRepository.getCustomerRunningTotals(startDateTime, endDateTime);

        return results.stream()
                .map(result -> CustomerRunningTotalDTO.builder()
                        .customerId(((Number) result[0]).longValue())
                        .orderDate(result[1] instanceof java.time.Instant
                                ? ((java.time.Instant) result[1]).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                                : (LocalDateTime) result[1])
                        .totalAmount(result[2] instanceof Number
                                ? java.math.BigDecimal.valueOf(((Number) result[2]).doubleValue())
                                : (java.math.BigDecimal) result[2])
                        .runningTotal(result[3] instanceof Number
                                ? java.math.BigDecimal.valueOf(((Number) result[3]).doubleValue())
                                : (java.math.BigDecimal) result[3])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Find top customers by spending
     *
     * @param limit number of top customers
     * @return list of top customers
     */
    public List<HighValueCustomerDTO> findTopCustomers(int limit) {
        log.info("Finding top {} customers by spending", limit);

        List<Object[]> results = orderRepository.findTopCustomers(limit);

        return results.stream()
                .map(result -> {
                    long customerId = ((Number) result[0]).longValue();
                    int orderCount = ((Number) result[1]).intValue();
                    double totalSpent = ((Number) result[2]).doubleValue();
                    double avgOrderValue = totalSpent / orderCount;

                    return HighValueCustomerDTO.builder()
                            .customerId(customerId)
                            .orderCount(orderCount)
                            .totalSpent(java.math.BigDecimal.valueOf(totalSpent))
                            .customerName("Customer " + customerId)
                            .customerEmail("customer" + customerId + "@email.com")
                            .avgOrderValue(java.math.BigDecimal.valueOf(avgOrderValue))
                            .build();
                })
                .collect(Collectors.toList());
    }
}
