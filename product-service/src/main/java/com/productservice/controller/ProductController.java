package com.productservice.controller;

import com.productservice.dto.ProductRequestDTO;
import com.productservice.dto.ProductResponseDTO;
import com.productservice.dto.ProductUpdateDTO;
import com.productservice.service.ProductService;
import com.productservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * REST Controller for Product Service
 * Provides REST API endpoints for product catalog management
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProductController {

    private final ProductService productService;
    private final SecurityUtils securityUtils;

    /**
     * Get all products
     * @return list of all products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        log.info("GET /api/products - Get all products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Get product by ID
     * @param id product ID
     * @return product with given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - Get product by ID", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Get active products - Demonstrates filtering
     * @return list of active products with stock
     */
    @GetMapping("/active")
    public ResponseEntity<List<ProductResponseDTO>> getActiveProducts() {
        log.info("GET /api/products/active - Get active products");
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    /**
     * Get products by category
     * @param categoryId category ID
     * @return list of products in category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        log.info("GET /api/products/category/{} - Get products by category", categoryId);
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    /**
     * Search products by name
     * @param name search term
     * @return list of matching products
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(@RequestParam String name) {
        log.info("GET /api/products/search?name={} - Search products", name);
        return ResponseEntity.ok(productService.searchProductsByName(name));
    }

    /**
     * Get product by SKU
     * @param sku product SKU
     * @return product with matching SKU
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponseDTO> getProductBySku(@PathVariable String sku) {
        log.info("GET /api/products/sku/{} - Get product by SKU", sku);
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    /**
     * Update product stock - Transaction Management Example
     * Requires authentication (USER or ADMIN)
     * @param id product ID
     * @param quantity quantity to update
     * @return updated product
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponseDTO> updateStock(
            @PathVariable Long id,
            @RequestParam @NotNull Integer quantity) {
        log.info("PATCH /api/products/{}/stock - Update stock by {}", id, quantity);

        // Require authentication
        if (!securityUtils.isAuthenticated()) {
            log.warn("Unauthorized attempt to update stock");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(productService.updateStock(id, quantity));
    }


    /**
     * Health check endpoint
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Product Service is healthy");
    }

    /**
     * Create a new product (ADMIN and PRODUCT_MANAGER only)
     * @param requestDTO product request DTO
     * @return created product response DTO
     */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        log.info("POST /api/products - Create product (ADMIN/PRODUCT_MANAGER)");

        // Check if user has ADMIN or PRODUCT_MANAGER role
        if (!securityUtils.canManageProducts()) {
            log.warn("Unauthorized attempt to create product by user without required role");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProductResponseDTO createdProduct = productService.createProduct(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Update an existing product (ADMIN and PRODUCT_MANAGER only)
     * @param id product ID
     * @param updateDTO product update DTO
     * @return updated product response DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDTO updateDTO) {
        log.info("PUT /api/products/{} - Update product (ADMIN/PRODUCT_MANAGER)", id);

        // Check if user has ADMIN or PRODUCT_MANAGER role
        if (!securityUtils.canManageProducts()) {
            log.warn("Unauthorized attempt to update product by user without required role");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProductResponseDTO updatedProduct = productService.updateProduct(id, updateDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Delete a product (ADMIN and PRODUCT_MANAGER only)
     * @param id product ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{} - Delete product (ADMIN/PRODUCT_MANAGER)", id);

        // Check if user has ADMIN or PRODUCT_MANAGER role
        if (!securityUtils.canManageProducts()) {
            log.warn("Unauthorized attempt to delete product by user without required role");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
