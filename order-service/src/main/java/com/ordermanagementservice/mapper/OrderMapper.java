package com.ordermanagementservice.mapper;

import com.ordermanagementservice.dto.OrderItemRequestDTO;
import com.ordermanagementservice.dto.OrderItemResponseDTO;
import com.ordermanagementservice.dto.OrderRequestDTO;
import com.ordermanagementservice.dto.OrderResponseDTO;
import com.ordermanagementservice.entity.Order;
import com.ordermanagementservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct Mapper for Order entity
 * Demonstrates MapStruct for DTO-Entity mapping
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    /**
     * Convert Order entity to OrderResponseDTO
     * @param order order entity
     * @return order response DTO
     */
    OrderResponseDTO toResponseDTO(Order order);

    /**
     * Convert OrderItem entity to OrderItemResponseDTO
     * @param item order item entity
     * @return order item response DTO
     */
    OrderItemResponseDTO toItemResponseDTO(OrderItem item);

    /**
     * Convert OrderRequestDTO to Order entity
     * @param dto order request DTO
     * @return order entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequestDTO dto);

    /**
     * Convert OrderItemRequestDTO to OrderItem entity
     * @param dto order item request DTO
     * @return order item entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toItemEntity(OrderItemRequestDTO dto);

    /**
     * Convert list of Order entities to DTOs
     * @param orders list of order entities
     * @return list of order response DTOs
     */
    List<OrderResponseDTO> toResponseDTOList(List<Order> orders);

    /**
     * Convert list of OrderItem entities to DTOs
     * @param items list of order item entities
     * @return list of order item response DTOs
     */
    List<OrderItemResponseDTO> toItemResponseDTOList(List<OrderItem> items);
}
