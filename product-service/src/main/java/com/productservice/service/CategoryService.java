package com.productservice.service;

import com.productservice.dto.CategoryRequestDTO;
import com.productservice.dto.CategoryResponseDTO;
import com.productservice.dto.CategoryUpdateDTO;
import com.productservice.entity.Category;
import com.productservice.exception.ResourceNotFoundException;
import com.productservice.mapper.CategoryMapper;
import com.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for Category business logic
 * Demonstrates Spring IoC and Service Layer pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Get all categories
     * @return list of all category response DTOs
     */
    public List<CategoryResponseDTO> getAllCategories() {
        log.debug("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toResponseDTOList(categories);
    }

    /**
     * Get category by ID
     * @param id category ID
     * @return category response DTO
     */
    public CategoryResponseDTO getCategoryById(Long id) {
        log.debug("Fetching category by id: {}", id);
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return categoryMapper.toResponseDTO(category);
    }

    /**
     * Search categories by name
     * @param name search term
     * @return list of matching category response DTOs
     */
    public List<CategoryResponseDTO> searchCategoriesByName(String name) {
        log.debug("Searching categories with name containing: {}", name);
        List<Category> categories = categoryRepository.findByNameContaining(name);
        return categoryMapper.toResponseDTOList(categories);
    }

    /**
     * Create a new category
     * @param requestDTO category request DTO
     * @return created category response DTO
     */
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO) {
        log.info("Creating new category: {}", requestDTO.getName());

        // Check if category name already exists
        if (categoryRepository.existsByName(requestDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + requestDTO.getName() + "' already exists");
        }

        Category category = categoryMapper.toEntity(requestDTO);
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return categoryMapper.toResponseDTO(savedCategory);
    }

    /**
     * Update an existing category
     * @param id category ID
     * @param updateDTO category update DTO
     * @return updated category response DTO
     */
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO updateDTO) {
        log.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        // Check if name is being changed and if it already exists
        if (updateDTO.getName() != null && !updateDTO.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(updateDTO.getName())) {
                throw new IllegalArgumentException("Category with name '" + updateDTO.getName() + "' already exists");
            }
        }

        // Manual partial update - only update fields that are not null
        if (updateDTO.getName() != null) {
            category.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            category.setDescription(updateDTO.getDescription());
        }

        Category updatedCategory = categoryRepository.save(category);

        log.info("Category updated successfully: {}", updatedCategory.getId());
        return categoryMapper.toResponseDTO(updatedCategory);
    }

    /**
     * Delete a category
     * @param id category ID
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted successfully: {}", id);
    }
}