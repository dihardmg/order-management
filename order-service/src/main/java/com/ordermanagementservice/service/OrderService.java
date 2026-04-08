package com.ordermanagementservice.service;

import com.ordermanagementservice.dto.OrderItemRequestDTO;
import com.ordermanagementservice.dto.OrderRequestDTO;
import com.ordermanagementservice.dto.OrderResponseDTO;
import com.ordermanagementservice.dto.OrderStatsDTO;
import com.ordermanagementservice.dto.ProductResponseDTO;
import com.ordermanagementservice.entity.Order;
import com.ordermanagementservice.entity.OrderItem;
import com.ordermanagementservice.entity.OrderStatus;
import com.ordermanagementservice.exception.ResourceNotFoundException;
import com.ordermanagementservice.mapper.OrderMapper;
import com.ordermanagementservice.repository.OrderItemRepository;
import com.ordermanagementservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for Order business logic
 * Demonstrates Spring IoC, Transaction Management, and Java Stream usage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductServiceClient productServiceClient;
    private final OrderItemRepository orderItemRepository;

    /**
     * Create new order - Transaction Management Example
     * Uses @CacheEvict to clear cache when new order is created
     * @param request order request DTO
     * @return created order response
     */
    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // Business logic: Validate customer exists
        if (request.getCustomerId() == null || request.getCustomerId() <= 0) {
            throw new ResourceNotFoundException("Customer", "id", request.getCustomerId());
        }

        // Validate order items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        // Create order entity
        Order order = orderMapper.toEntity(request);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Calculate total amount by fetching product prices from Product Service
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDTO itemRequest : request.getItems()) {
            // Fetch product from Product Service
            ProductResponseDTO product = productServiceClient.getProductById(itemRequest.getProductId());

            // Validate product is available
            if (product.getIsActive() == null || !product.getIsActive()) {
                throw new IllegalArgumentException("Product " + product.getName() + " is not available");
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() +
                    ". Available: " + product.getStock() + ", Requested: " + itemRequest.getQuantity());
            }

            // Calculate item total
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // Create order item with price
            OrderItem orderItem = OrderItem.builder()
                .productId(itemRequest.getProductId())
                .quantity(itemRequest.getQuantity())
                .price(product.getPrice())
                .orderId(null) // Will be set after order is saved
                .build();

            orderItems.add(orderItem);

            log.info("Added item: {} x {} = {}", product.getName(), itemRequest.getQuantity(), itemTotal);
        }

        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

        // Save order without items first
        Order savedOrder = orderRepository.save(order);

        // Save order items with correct order ID
        for (OrderItem item : orderItems) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);
        }

        // Deduct stock from Product Service
        log.info("Deducting stock for order items...");
        for (OrderItem item : orderItems) {
            try {
                productServiceClient.updateProductStock(item.getProductId(), -item.getQuantity());
                log.info("Successfully deducted {} units from product {}", item.getQuantity(), item.getProductId());
            } catch (Exception e) {
                log.error("Failed to deduct stock for product {}: {}", item.getProductId(), e.getMessage());
                // Rollback: delete order and items since stock deduction failed
                orderItemRepository.deleteAll(orderItems);
                orderRepository.delete(savedOrder);
                throw new RuntimeException("Failed to deduct stock for product: " + item.getProductId() + ". Order rolled back.", e);
            }
        }

        log.info("Order created successfully with ID: {}, Total Amount: {}", savedOrder.getId(), savedOrder.getTotalAmount());
        return orderMapper.toResponseDTO(savedOrder);
    }

    /**
     * Get all orders
     * @return list of all order response DTOs
     */
    public List<OrderResponseDTO> getAllOrders() {
        log.debug("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toResponseDTOList(orders);
    }

    /**
     * Get order by ID with proper exception handling
     * Uses @Cacheable to cache frequently accessed orders
     * @param id order ID
     * @return order response DTO
     */
    @Cacheable(value = "orders", key = "#id")
    public OrderResponseDTO getOrderById(Long id) {
        log.debug("Fetching order by id: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return orderMapper.toResponseDTO(order);
    }

    /**
     * Get orders by customer ID - Java Stream Example
     * @param customerId customer ID
     * @return list of customer order DTOs
     */
    public List<OrderResponseDTO> getOrdersByCustomerId(Long customerId) {
        log.debug("Fetching orders for customer: {}", customerId);

        // Java Stream API demonstration - filter out cancelled orders
        List<Order> orders = orderRepository.findByCustomerId(customerId).stream()
            .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
            .collect(Collectors.toList());

        return orderMapper.toResponseDTOList(orders);
    }

    /**
     * Get orders by date range - Java Stream Processing with Sorting
     * @param startDate start date
     * @param endDate end date
     * @return list of order DTOs within date range
     */
    public List<OrderResponseDTO> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching orders between {} and {}", startDate, endDate);

        // Convert LocalDate to LocalDateTime for full day coverage
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Complex Stream operation - filter by date range and sort by date descending
        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime).stream()
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .collect(Collectors.toList());

        return orderMapper.toResponseDTOList(orders);
    }

    /**
     * Get orders by status
     * @param status order status
     * @return list of order DTOs with given status
     */
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        log.debug("Fetching orders with status: {}", status);
        List<Order> orders = orderRepository.findByStatus(status);
        return orderMapper.toResponseDTOList(orders);
    }

    /**
     * Get order statistics - Java Stream Aggregation Example
     * Demonstrates complex Stream operations for data analysis
     * @param startDate start date
     * @param endDate end date
     * @return order statistics DTO
     */
    public OrderStatsDTO getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating order statistics from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

        // Java Stream API aggregation operations
        long totalOrders = orders.stream().count();

        BigDecimal totalRevenue = orders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = totalOrders > 0
            ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // Group by status using Stream groupingBy
        Map<OrderStatus, Long> statusBreakdown = orders.stream()
            .collect(Collectors.groupingBy(
                Order::getStatus,
                Collectors.counting()
            ));

        return OrderStatsDTO.builder()
            .totalOrders(totalOrders)
            .totalRevenue(totalRevenue)
            .averageOrderValue(averageOrderValue)
            .statusBreakdown(statusBreakdown)
            .startDate(startDate.toString())
            .endDate(endDate.toString())
            .build();
    }

    /**
     * Update order status - Transaction Management
     * Uses @CachePut to update cache when order is modified
     * @param orderId order ID
     * @param newStatus new status
     * @return updated order response
     */
    @Transactional
    @CachePut(value = "orders", key = "#orderId")
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Business logic: Validate status transition
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new IllegalArgumentException(
                String.format("Cannot transition from %s to %s", order.getStatus(), newStatus));
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated successfully", orderId);

        return orderMapper.toResponseDTO(updatedOrder);
    }

    /**
     * Validate order status transition
     * Business logic for order workflow
     */
    private boolean isValidStatusTransition(OrderStatus current, OrderStatus newStatus) {
        // Define valid status transitions
        if (current == OrderStatus.PENDING) {
            return newStatus == OrderStatus.PROCESSING ||
                   newStatus == OrderStatus.CANCELLED;
        }
        if (current == OrderStatus.PROCESSING) {
            return newStatus == OrderStatus.SHIPPED ||
                   newStatus == OrderStatus.CANCELLED;
        }
        if (current == OrderStatus.SHIPPED) {
            return newStatus == OrderStatus.DELIVERED;
        }
        return false;
    }
}
