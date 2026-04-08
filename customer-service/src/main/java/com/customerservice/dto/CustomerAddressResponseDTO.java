package com.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for customer address response
 * Used to return address data via REST API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddressResponseDTO {

    private Long id;
    private Long customerId;
    private String addressLine;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private Boolean isDefault;
}
