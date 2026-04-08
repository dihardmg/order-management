package com.customerservice.mapper;

import com.customerservice.dto.CustomerAddressResponseDTO;
import com.customerservice.dto.CustomerRequestDTO;
import com.customerservice.dto.CustomerResponseDTO;
import com.customerservice.entity.Customer;
import com.customerservice.entity.CustomerAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Customer entity
 * Demonstrates MapStruct for DTO-Entity mapping
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /**
     * Convert Customer entity to CustomerResponseDTO
     * @param customer customer entity
     * @return customer response DTO
     */
    CustomerResponseDTO toResponseDTO(Customer customer);

    /**
     * Convert CustomerAddress entity to CustomerAddressResponseDTO
     * @param address customer address entity
     * @return customer address response DTO
     */
    CustomerAddressResponseDTO toAddressResponseDTO(CustomerAddress address);

    /**
     * Convert list of Customer entities to DTOs
     * @param customers list of customer entities
     * @return list of customer response DTOs
     */
    List<CustomerResponseDTO> toResponseDTOList(List<Customer> customers);

    /**
     * Convert list of CustomerAddress entities to DTOs
     * @param addresses list of customer address entities
     * @return list of customer address response DTOs
     */
    List<CustomerAddressResponseDTO> toAddressResponseDTOList(List<CustomerAddress> addresses);

    /**
     * Convert CustomerRequestDTO to Customer entity
     * @param requestDTO customer request DTO
     * @return customer entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    Customer toEntity(CustomerRequestDTO requestDTO);
}
