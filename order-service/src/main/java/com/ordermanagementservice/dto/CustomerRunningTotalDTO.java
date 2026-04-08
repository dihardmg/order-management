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
public class CustomerRunningTotalDTO {

    private Long customerId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private BigDecimal runningTotal;
}
