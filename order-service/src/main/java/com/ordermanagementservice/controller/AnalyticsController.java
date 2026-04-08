package com.ordermanagementservice.controller;

import com.ordermanagementservice.dto.CustomerRunningTotalDTO;
import com.ordermanagementservice.dto.HighValueCustomerDTO;
import com.ordermanagementservice.dto.OrderSummaryDTO;
import com.ordermanagementservice.dto.SalesReportDTO;
import com.ordermanagementservice.entity.OrderStatus;
import com.ordermanagementservice.service.AnalyticsService;
import com.ordermanagementservice.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Order Analytics and Reporting
 * Demonstrates advanced SQL queries, Java Stream processing, and caching
 */
@RestController
@RequestMapping("/api/orders/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityUtils securityUtils;

    /**
     * Verify ADMIN access - returns true if admin, throws exception otherwise
     * @return true if user has ADMIN role
     */
    private boolean requireAdmin() {
        if (!securityUtils.isAdmin()) {
            log.warn("Unauthorized attempt to access analytics by non-admin user");
            throw new IllegalStateException("Access denied. ADMIN role required.");
        }
        return true;
    }

    // ============================================================
    // JAVA STREAM PROCESSING ENDPOINTS
    // ============================================================

    /**
     * Get order status breakdown
     * Demonstrates Java Stream groupingBy and counting
     *
     * Example: GET /api/orders/analytics/status-breakdown?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/status-breakdown")
    public ResponseEntity<Map<OrderStatus, Long>> getOrderStatusBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        requireAdmin(); // ADMIN only

        log.info("Generating order status breakdown from {} to {}", startDate, endDate);
        Map<OrderStatus, Long> breakdown = analyticsService.getOrderStatusBreakdown(startDate, endDate);
        return ResponseEntity.ok(breakdown);
    }

    /**
     * Find high-value customers
     * Demonstrates Java Stream map, filter, sorted operations
     *
     * Example: GET /api/orders/analytics/high-value-customers?minPurchase=1000000
     */
    @GetMapping("/high-value-customers")
    public ResponseEntity<List<HighValueCustomerDTO>> findHighValueCustomers(
            @RequestParam(defaultValue = "1000000") double minPurchase) {

        requireAdmin(); // ADMIN only

        log.info("Finding high-value customers with minimum purchase: {}", minPurchase);
        List<HighValueCustomerDTO> customers = analyticsService.findHighValueCustomers(minPurchase);
        return ResponseEntity.ok(customers);
    }

    /**
     * Generate order summaries for a specific date
     * Demonstrates Java Stream map, filter, sorted, limit
     *
     * Example: GET /api/orders/analytics/summaries?date=2026-04-06
     */
    @GetMapping("/summaries")
    public ResponseEntity<List<OrderSummaryDTO>> generateOrderSummaries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Generating order summaries for date: {}", date);
        List<OrderSummaryDTO> summaries = analyticsService.generateOrderSummaries(date);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Calculate daily revenue for a date range
     * Demonstrates Java Stream groupingBy and summing
     *
     * Example: GET /api/orders/analytics/daily-revenue?startDate=2026-04-01&endDate=2026-04-30
     */
    @GetMapping("/daily-revenue")
    public ResponseEntity<Map<LocalDate, java.math.BigDecimal>> calculateDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Calculating daily revenue from {} to {}", startDate, endDate);
        Map<LocalDate, java.math.BigDecimal> revenue = analyticsService.calculateDailyRevenue(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    // ============================================================
    // ADVANCED NATIVE SQL QUERY ENDPOINTS
    // ============================================================

    /**
     * Generate monthly sales report with CTE query
     * Demonstrates PostgreSQL Common Table Expressions
     *
     * Example: GET /api/orders/analytics/monthly-report?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/monthly-report")
    public ResponseEntity<List<SalesReportDTO>> generateMonthlySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        requireAdmin(); // ADMIN only

        log.info("Generating monthly sales report from {} to {}", startDate, endDate);
        List<SalesReportDTO> report = analyticsService.generateMonthlySalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Get customer running totals using Window Functions
     * Demonstrates PostgreSQL Window Functions
     *
     * Example: GET /api/orders/analytics/running-totals?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/running-totals")
    public ResponseEntity<List<CustomerRunningTotalDTO>> getCustomerRunningTotals(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting customer running totals from {} to {}", startDate, endDate);
        List<CustomerRunningTotalDTO> totals = analyticsService.getCustomerRunningTotals(startDate, endDate);
        return ResponseEntity.ok(totals);
    }

    /**
     * Find top customers by spending
     * Demonstrates SQL GROUP BY, HAVING, ORDER BY
     *
     * Example: GET /api/orders/analytics/top-customers?limit=10
     */
    @GetMapping("/top-customers")
    public ResponseEntity<List<HighValueCustomerDTO>> findTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Finding top {} customers by spending", limit);
        List<HighValueCustomerDTO> customers = analyticsService.findTopCustomers(limit);
        return ResponseEntity.ok(customers);
    }

    // ============================================================
    // PERFORMANCE & CACHING DEMONSTRATION ENDPOINTS
    // ============================================================
}
