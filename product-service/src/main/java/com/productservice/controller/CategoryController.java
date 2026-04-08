package com.productservice.controller;

import com.productservice.dto.CategoryRequestDTO;
import com.productservice.dto.CategoryResponseDTO;
import com.productservice.dto.CategoryUpdateDTO;
import com.productservice.service.CategoryService;
import com.productservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Category Service
 * Provides REST API endpoints for category management
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    /**
     * Get all categories
     * @return list of all categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        log.info("GET /api/categories - Get all categories");
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Get category by ID
     * @param id category ID
     * @return category with given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        log.info("GET /api/categories/{} - Get category by ID", id);
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * Search categories by name - Demonstrates database search
     * @param name search term
     * @return list of matching categories
     */
    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponseDTO>> searchCategoriesByName(@RequestParam String name) {
        log.info("GET /api/categories/search?name={} - Search categories", name);
        return ResponseEntity.ok(categoryService.searchCategoriesByName(name));
    }

    /**
     * Health check endpoint
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Category Service is healthy");
    }

    /**
     * Create a new category (ADMIN and PRODUCT_MANAGER only)
     * @param requestDTO category request DTO
     * @return created category response DTO
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        log.info("POST /api/categories - Create category (ADMIN/PRODUCT_MANAGER)");

        // Check if user has ADMIN or PRODUCT_MANAGER role
        if (!securityUtils.canManageProducts()) {
            log.warn("Unauthorized attempt to create category by user without required role");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CategoryResponseDTO createdCategory = categoryService.createCategory(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    /**
     * Update an existing category (ADMIN and PRODUCT_MANAGER only)
     * @param id category ID
     * @param updateDTO category update DTO
     * @return updated category response DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateDTO updateDTO) {
        log.info("PUT /api/categories/{} - Update category (ADMIN/PRODUCT_MANAGER)", id);

        // Check if user has ADMIN or PRODUCT_MANAGER role
        if (!securityUtils.canManageProducts()) {
            log.warn("Unauthorized attempt to update category by user without required role");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, updateDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Delete a category (ADMIN and PRODUCT_MANAGER only)
     * @param id category ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("DELETE /api/categories/{} - Delete category (ADMIN/PRODUCT_MANAGER)", id);

        // Check if user has ADMIN or PRODUCT_MANAGER role
        if (!securityUtils.canManageProducts()) {
            log.warn("Unauthorized attempt to delete category by user without required role");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}