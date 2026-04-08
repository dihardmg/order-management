package com.ordermanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDTO {

    private LocalDate reportDate;    // Date of the report (was: month)
    private Long orderCount;         // Total number of orders
    private BigDecimal revenue;      // Total revenue
    private Long uniqueCustomers;    // Number of unique customers
    private BigDecimal avgOrderValue; // Average order value
}
