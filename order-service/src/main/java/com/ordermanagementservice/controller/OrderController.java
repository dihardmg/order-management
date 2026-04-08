package com.ordermanagementservice.controller;

import com.ordermanagementservice.dto.OrderRequestDTO;
import com.ordermanagementservice.dto.OrderResponseDTO;
import com.ordermanagementservice.dto.OrderStatsDTO;
import com.ordermanagementservice.entity.OrderStatus;
import com.ordermanagementservice.service.OrderService;
import com.ordermanagementservice.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Order Service
 * Provides REST API endpoints for order management with validation
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Orders", description = "Order Management APIs - Create, read, update orders and manage order lifecycle")
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    /**
     * Create new order - POST endpoint with validation
     * Requires authentication (USER or ADMIN)
     * @param request order request DTO
     * @return created order response
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO request) {
        log.info("POST /api/orders - Create order for customer: {}", request.getCustomerId());

        // Require authentication
        securityUtils.requireAuthentication();

        OrderResponseDTO response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all orders
     * @return list of all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        log.info("GET /api/orders - Get all orders");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * Get order by ID
     * @param id order ID
     * @return order with given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        log.info("GET /api/orders/{} - Get order by ID", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * Get customer orders - Demonstrates service discovery
     * Requires authentication. Users can only see their own orders, admins can see all.
     * @param customerId customer ID
     * @return list of customer orders
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getCustomerOrders(@PathVariable Long customerId) {
        log.info("GET /api/orders/customer/{} - Get customer orders", customerId);

        // Require authentication and check access
        securityUtils.requireAuthentication();
        if (!securityUtils.canAccessOrder(customerId)) {
            log.warn("Unauthorized attempt to access orders for customer: {}", customerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    /**
     * Get orders by date range
     * @param startDate start date (ISO format: yyyy-MM-dd)
     * @param endDate end date (ISO format: yyyy-MM-dd)
     * @return list of orders within date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /api/orders/date-range?startDate={}&endDate={} - Get orders by date range", startDate, endDate);
        return ResponseEntity.ok(orderService.getOrdersByDateRange(startDate, endDate));
    }

    /**
     * Get orders by status
     * @param status order status
     * @return list of orders with given status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("GET /api/orders/status/{} - Get orders by status", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    /**
     * Update order status - Business logic endpoint
     * Requires ADMIN role
     * @param id order ID
     * @param status new status
     * @return updated order
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        log.info("PATCH /api/orders/{}/status - Update status to {}", id, status);

        // Require ADMIN role
        if (!securityUtils.isAdmin()) {
            log.warn("Unauthorized attempt to update order status by non-admin user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    /**
     * Get order statistics - Java Stream Aggregation Demo
     * Requires ADMIN role
     * @param startDate start date
     * @param endDate end date
     * @return order statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<OrderStatsDTO> getOrderStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/orders/statistics?startDate={}&endDate={} - Get order statistics", startDate, endDate);

        // Require ADMIN role
        if (!securityUtils.isAdmin()) {
            log.warn("Unauthorized attempt to access order statistics by non-admin user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orderService.getOrderStatistics(startDate, endDate));
    }

    /**
     * Health check endpoint
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is healthy");
    }
}
