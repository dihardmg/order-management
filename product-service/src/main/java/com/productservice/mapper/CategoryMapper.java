package com.productservice.mapper;

import com.productservice.dto.CategoryRequestDTO;
import com.productservice.dto.CategoryResponseDTO;
import com.productservice.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Category entity
 * Demonstrates MapStruct for DTO-Entity mapping
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Convert Category entity to CategoryResponseDTO
     * @param category category entity
     * @return category response DTO
     */
    CategoryResponseDTO toResponseDTO(Category category);

    /**
     * Convert list of Category entities to DTOs
     * @param categories list of category entities
     * @return list of category response DTOs
     */
    List<CategoryResponseDTO> toResponseDTOList(List<Category> categories);

    /**
     * Convert CategoryRequestDTO to Category entity
     * @param requestDTO category request DTO
     * @return category entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryRequestDTO requestDTO);
}