package com.productservice.mapper;

import com.productservice.dto.ProductRequestDTO;
import com.productservice.dto.ProductResponseDTO;
import com.productservice.dto.ProductUpdateDTO;
import com.productservice.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Product entity
 * Demonstrates MapStruct for DTO-Entity mapping
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Convert Product entity to ProductResponseDTO
     * @param product product entity
     * @return product response DTO
     */
    ProductResponseDTO toResponseDTO(Product product);

    /**
     * Convert list of Product entities to DTOs
     * @param products list of product entities
     * @return list of product response DTOs
     */
    List<ProductResponseDTO> toResponseDTOList(List<Product> products);

    /**
     * Convert ProductRequestDTO to Product entity
     * @param requestDTO product request DTO
     * @return product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequestDTO requestDTO);
}
