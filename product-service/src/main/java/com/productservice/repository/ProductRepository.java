package com.productservice.repository;

import com.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Product entity
 * Provides database access methods for Product operations
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by category ID
     * @param categoryId the category ID
     * @return list of products in the category
     */
    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Find active products
     * @param isActive active status
     * @return list of active products
     */
    List<Product> findByIsActive(Boolean isActive);

    /**
     * Find products by SKU
     * @param sku the product SKU
     * @return product with matching SKU
     */
    Product findBySku(String sku);

    /**
     * Find products by name containing search term
     * @param name search term
     * @return list of matching products
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContaining(@Param("name") String name);

    /**
     * Count products by category
     * @param categoryId the category ID
     * @return number of products in category
     */
    long countByCategoryId(Long categoryId);
}
