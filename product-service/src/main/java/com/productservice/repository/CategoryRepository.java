package com.productservice.repository;

import com.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Category entity
 * Provides database access methods for Category operations
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find categories by name containing search term
     * @param name search term
     * @return list of matching categories
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> findByNameContaining(@Param("name") String name);

    /**
     * Check if category name exists
     * @param name the category name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
}
