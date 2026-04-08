package com.customerservice.repository;

import com.customerservice.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for CustomerAddress entity
 * Provides database access methods for CustomerAddress operations
 */
@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    /**
     * Find all addresses for a specific customer
     * @param customerId the customer ID
     * @return list of customer addresses
     */
    List<CustomerAddress> findByCustomerId(Long customerId);

    /**
     * Find customer's default address
     * @param customerId the customer ID
     * @return default address or null
     */
    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customerId = :customerId AND ca.isDefault = true")
    CustomerAddress findDefaultAddressByCustomerId(@Param("customerId") Long customerId);

    /**
     * Count addresses for a customer
     * @param customerId the customer ID
     * @return number of addresses
     */
    long countByCustomerId(Long customerId);
}
