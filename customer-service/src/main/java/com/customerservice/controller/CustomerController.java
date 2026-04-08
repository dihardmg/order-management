package com.customerservice.controller;

import com.customerservice.dto.CustomerRequestDTO;
import com.customerservice.dto.CustomerResponseDTO;
import com.customerservice.dto.CustomerUpdateDTO;
import com.customerservice.service.CustomerService;
import com.customerservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Customer Service
 * Provides REST API endpoints for customer management
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CustomerController {

    private final CustomerService customerService;
    private final SecurityUtils securityUtils;

    /**
     * Get all customers
     * @return list of all customers
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        log.info("GET /api/customers - Get all customers");
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    /**
     * Get customer by ID
     * @param id customer ID
     * @return customer with given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        log.info("GET /api/customers/{} - Get customer by ID", id);
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    /**
     * Get customer by email
     * @param email customer email
     * @return customer with given email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponseDTO> getCustomerByEmail(@PathVariable String email) {
        log.info("GET /api/customers/email/{} - Get customer by email", email);
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    /**
     * Search customers by name - Demonstrates database search
     * @param name search term
     * @return list of matching customers
     */
    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponseDTO>> searchCustomersByName(@RequestParam String name) {
        log.info("GET /api/customers/search?name={} - Search customers", name);
        return ResponseEntity.ok(customerService.searchCustomersByName(name));
    }

    /**
     * Check if customer email exists
     * @param email customer email
     * @return true if exists, false otherwise
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        log.info("GET /api/customers/exists?email={} - Check email exists", email);
        return ResponseEntity.ok(customerService.existsByEmail(email));
    }

    /**
     * Health check endpoint
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer Service is healthy");
    }

    /**
     * Create a new customer (ADMIN ONLY)
     * @param requestDTO customer request DTO
     * @return created customer response DTO
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO requestDTO) {
        log.info("POST /api/customers - Create customer (ADMIN)");

        // Check if user has ADMIN role
        if (!securityUtils.isAdmin()) {
            log.warn("Unauthorized attempt to create customer by non-admin user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CustomerResponseDTO createdCustomer = customerService.createCustomer(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    /**
     * Update an existing customer (ADMIN ONLY)
     * @param id customer ID
     * @param updateDTO customer update DTO
     * @return updated customer response DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateDTO updateDTO) {
        log.info("PUT /api/customers/{} - Update customer (ADMIN)", id);

        // Check if user has ADMIN role
        if (!securityUtils.isAdmin()) {
            log.warn("Unauthorized attempt to update customer by non-admin user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, updateDTO);
        return ResponseEntity.ok(updatedCustomer);
    }

    /**
     * Delete a customer (ADMIN ONLY)
     * @param id customer ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("DELETE /api/customers/{} - Delete customer (ADMIN)", id);

        // Check if user has ADMIN role
        if (!securityUtils.isAdmin()) {
            log.warn("Unauthorized attempt to delete customer by non-admin user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}