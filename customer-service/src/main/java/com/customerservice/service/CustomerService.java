package com.customerservice.service;

import com.customerservice.dto.CustomerRequestDTO;
import com.customerservice.dto.CustomerResponseDTO;
import com.customerservice.dto.CustomerUpdateDTO;
import com.customerservice.entity.Customer;
import com.customerservice.exception.ResourceNotFoundException;
import com.customerservice.mapper.CustomerMapper;
import com.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for Customer business logic
 * Demonstrates Spring IoC and database operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    /**
     * Get all customers
     * @return list of all customer response DTOs
     */
    public List<CustomerResponseDTO> getAllCustomers() {
        log.debug("Fetching all customers");
        List<Customer> customers = customerRepository.findAll();
        return customerMapper.toResponseDTOList(customers);
    }

    /**
     * Get customer by ID
     * @param id customer ID
     * @return customer response DTO
     */
    public CustomerResponseDTO getCustomerById(Long id) {
        log.debug("Fetching customer by id: {}", id);
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        return customerMapper.toResponseDTO(customer);
    }

    /**
     * Get customer by email
     * @param email customer email
     * @return customer response DTO
     */
    public CustomerResponseDTO getCustomerByEmail(String email) {
        log.debug("Fetching customer by email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return customerMapper.toResponseDTO(customer);
    }

    /**
     * Search customers by name - Java Stream Example
     * @param name search term
     * @return list of matching customer response DTOs
     */
    public List<CustomerResponseDTO> searchCustomersByName(String name) {
        log.debug("Searching customers with name containing: {}", name);
        List<Customer> customers = customerRepository.findByNameContaining(name);
        return customerMapper.toResponseDTOList(customers);
    }

    /**
     * Check if customer exists by email
     * @param email customer email
     * @return true if exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    /**
     * Create a new customer
     * @param requestDTO customer request DTO
     * @return created customer response DTO
     */
    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {
        log.info("Creating new customer: {}", requestDTO.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("Customer with email " + requestDTO.getEmail() + " already exists");
        }

        Customer customer = customerMapper.toEntity(requestDTO);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return customerMapper.toResponseDTO(savedCustomer);
    }

    /**
     * Update an existing customer
     * @param id customer ID
     * @param updateDTO customer update DTO
     * @return updated customer response DTO
     */
    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerUpdateDTO updateDTO) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        // Check if email is being changed and if it already exists
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(updateDTO.getEmail())) {
                throw new IllegalArgumentException("Customer with email " + updateDTO.getEmail() + " already exists");
            }
        }

        // Manual partial update - only update fields that are not null
        if (updateDTO.getName() != null) {
            customer.setName(updateDTO.getName());
        }
        if (updateDTO.getEmail() != null) {
            customer.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhone() != null) {
            customer.setPhone(updateDTO.getPhone());
        }

        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer updated successfully: {}", updatedCustomer.getId());
        return customerMapper.toResponseDTO(updatedCustomer);
    }

    /**
     * Delete a customer by ID
     * @param id customer ID
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", id);
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted successfully: {}", id);
    }
}
