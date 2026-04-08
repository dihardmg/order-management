package com.ordermanagementservice.dto;

import com.ordermanagementservice.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for order statistics
 * Demonstrates aggregation operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsDTO {

    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Map<OrderStatus, Long> statusBreakdown;
    private String startDate;
    private String endDate;
}
