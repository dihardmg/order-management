package com.ordermanagementservice.service;

import com.ordermanagementservice.dto.ProductResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service for communicating with Product Service
 * Uses RestTemplate with PATCH support for service-to-service communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    @Qualifier("restTemplate")
    private final RestTemplate restTemplate;

    @Value("${product.service.base-url:http://product-service}")
    private String productServiceBaseUrl;

    /**
     * Get current HTTP request with headers
     * @return HttpServletRequest or null
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * Create HttpEntity with forwarded headers from gateway
     * @param httpMethod the HTTP method to use
     * @return HttpEntity with headers
     */
    private HttpEntity<?> createHttpEntityWithHeaders(HttpMethod httpMethod) {
        HttpHeaders headers = new HttpHeaders();
        HttpServletRequest request = getCurrentRequest();

        if (request != null) {
            // Forward authentication headers from gateway
            String userId = request.getHeader("X-User-Id");
            String username = request.getHeader("X-Username");
            String email = request.getHeader("X-User-Email");
            String role = request.getHeader("X-User-Role");

            if (userId != null) headers.set("X-User-Id", userId);
            if (username != null) headers.set("X-Username", username);
            if (email != null) headers.set("X-User-Email", email);
            if (role != null) headers.set("X-User-Role", role);

            log.debug("Forwarding headers - X-User-Id: {}, X-User-Role: {}", userId, role);
        }

        return new HttpEntity<>(headers);
    }

    /**
     * Get product by ID from Product Service
     * @param productId product ID
     * @return product response DTO
     * @throws ResponseStatusException if product not found
     */
    public ProductResponseDTO getProductById(Long productId) {
        log.debug("Fetching product {} from Product Service", productId);

        try {
            String url = productServiceBaseUrl + "/api/products/" + productId;
            HttpEntity<?> entity = createHttpEntityWithHeaders(HttpMethod.GET);

            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ProductResponseDTO.class
            );

            ProductResponseDTO product = response.getBody();
            if (product == null) {
                throw new ResponseStatusException(NOT_FOUND, "Product not found with ID: " + productId);
            }

            log.debug("Product fetched successfully: {} - Price: {}", product.getName(), product.getPrice());
            return product;

        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new ResponseStatusException(NOT_FOUND, "Product not found with ID: " + productId);
        }
    }

    /**
     * Update product stock after order
     * @param productId product ID
     * @param quantity quantity to deduct (negative number)
     * @return updated product response
     */
    public ProductResponseDTO updateProductStock(Long productId, Integer quantity) {
        log.debug("Updating stock for product {} by {}", productId, quantity);

        try {
            String url = productServiceBaseUrl + "/api/products/" + productId + "/stock?quantity=" + quantity;
            HttpEntity<?> entity = createHttpEntityWithHeaders(HttpMethod.PATCH);

            var response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    ProductResponseDTO.class
            );

            ProductResponseDTO product = response.getBody();
            if (product == null) {
                throw new RuntimeException("Failed to update stock - null response");
            }

            log.debug("Stock updated successfully for product {}", productId);
            return product;

        } catch (Exception e) {
            log.error("Error updating stock for product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to update stock for product: " + productId, e);
        }
    }

    /**
     * Check if product exists and is active
     * @param productId product ID
     * @return true if product exists and is active
     */
    public boolean isProductAvailable(Long productId) {
        try {
            ProductResponseDTO product = getProductById(productId);
            return product.getIsActive() != null && product.getIsActive() && product.getStock() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
