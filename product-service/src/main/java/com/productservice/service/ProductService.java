package com.productservice.service;

import com.productservice.dto.ProductRequestDTO;
import com.productservice.dto.ProductResponseDTO;
import com.productservice.dto.ProductUpdateDTO;
import com.productservice.entity.Product;
import com.productservice.exception.ResourceNotFoundException;
import com.productservice.mapper.ProductMapper;
import com.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Product business logic
 * Demonstrates Spring IoC and Java Stream usage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Get all products
     * @return list of all product response DTOs
     */
    public List<ProductResponseDTO> getAllProducts() {
        log.debug("Fetching all products");
        List<Product> products = productRepository.findAll();
        return productMapper.toResponseDTOList(products);
    }

    /**
     * Get product by ID
     * @param id product ID
     * @return product response DTO
     */
    public ProductResponseDTO getProductById(Long id) {
        log.debug("Fetching product by id: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return productMapper.toResponseDTO(product);
    }

    /**
     * Update product stock - Transaction Management Example
     * @param productId product ID
     * @param quantity quantity to add (positive) or subtract (negative)
     * @return updated product response
     */
    @Transactional
    public ProductResponseDTO updateStock(Long productId, Integer quantity) {
        log.info("Updating stock for product {} by {}", productId, quantity);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        int newStock = product.getStock() + quantity;

        // Business logic validation
        if (newStock < 0) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock. Current: %d, Requested change: %d",
                    product.getStock(), quantity));
        }

        product.setStock(newStock);

        // Auto-update timestamp via @PreUpdate
        Product updatedProduct = productRepository.save(product);

        log.info("Stock updated successfully for product {}", productId);
        return productMapper.toResponseDTO(updatedProduct);
    }

    /**
     * Get active products - Java Stream Filter
     * @return list of active product response DTOs
     */
    public List<ProductResponseDTO> getActiveProducts() {
        log.debug("Fetching active products");

        // Java Stream API demonstration - filtering
        List<Product> products = productRepository.findByIsActive(true).stream()
            .filter(product -> product.getStock() > 0)
            .collect(Collectors.toList());

        return productMapper.toResponseDTOList(products);
    }

    /**
     * Get products by category
     * @param categoryId category ID
     * @return list of product response DTOs in category
     */
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        log.debug("Fetching products for category: {}", categoryId);
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return productMapper.toResponseDTOList(products);
    }

    /**
     * Search products by name
     * @param name search term
     * @return list of matching product response DTOs
     */
    public List<ProductResponseDTO> searchProductsByName(String name) {
        log.debug("Searching products with name containing: {}", name);
        List<Product> products = productRepository.findByNameContaining(name);
        return productMapper.toResponseDTOList(products);
    }

    /**
     * Get total product count
     * @return total number of products
     */
    public long getTotalProducts() {
        return productRepository.count();
    }

    /**
     * Get product by SKU
     * @param sku product SKU
     * @return product response DTO with matching SKU
     */
    public ProductResponseDTO getProductBySku(String sku) {
        log.debug("Fetching product by SKU: {}", sku);
        Product product = productRepository.findBySku(sku);
        if (product == null) {
            throw new ResourceNotFoundException("Product", "sku", sku);
        }
        return productMapper.toResponseDTO(product);
    }

    /**
     * Create a new product
     * @param requestDTO product request DTO
     * @return created product response DTO
     */
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        log.info("Creating new product: {}", requestDTO.getName());

        // Check if SKU already exists
        Product existingProduct = productRepository.findBySku(requestDTO.getSku());
        if (existingProduct != null) {
            throw new IllegalArgumentException("Product with SKU " + requestDTO.getSku() + " already exists");
        }

        Product product = productMapper.toEntity(requestDTO);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return productMapper.toResponseDTO(savedProduct);
    }

    /**
     * Update an existing product
     * @param id product ID
     * @param updateDTO product update DTO
     * @return updated product response DTO
     */
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO updateDTO) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // Check if SKU is being changed and if it already exists
        if (updateDTO.getSku() != null && !updateDTO.getSku().equals(product.getSku())) {
            Product existingProduct = productRepository.findBySku(updateDTO.getSku());
            if (existingProduct != null) {
                throw new IllegalArgumentException("Product with SKU " + updateDTO.getSku() + " already exists");
            }
        }

        // Manual partial update - only update fields that are not null
        if (updateDTO.getName() != null) {
            product.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            product.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getPrice() != null) {
            product.setPrice(updateDTO.getPrice());
        }
        if (updateDTO.getStock() != null) {
            product.setStock(updateDTO.getStock());
        }
        if (updateDTO.getCategoryId() != null) {
            product.setCategoryId(updateDTO.getCategoryId());
        }
        if (updateDTO.getSku() != null) {
            product.setSku(updateDTO.getSku());
        }
        if (updateDTO.getIsActive() != null) {
            product.setIsActive(updateDTO.getIsActive());
        }

        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully: {}", updatedProduct.getId());
        return productMapper.toResponseDTO(updatedProduct);
    }

    /**
     * Delete a product
     * @param id product ID
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully: {}", id);
    }
}
