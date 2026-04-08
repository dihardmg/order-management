package com.ordermanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighValueCustomerDTO {

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalSpent;
    private Integer orderCount;
    private BigDecimal avgOrderValue;
}
