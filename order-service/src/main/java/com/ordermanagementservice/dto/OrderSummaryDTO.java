package com.ordermanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {

    private Long orderId;
    private Long customerId;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private Integer itemCount;
}
