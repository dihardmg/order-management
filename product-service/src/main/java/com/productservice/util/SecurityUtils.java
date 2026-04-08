package com.productservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Security utility class for Product Service
 * Extracts user role from headers set by API Gateway
 */
@Component
@Slf4j
public class SecurityUtils {

    private static final String ROLE_HEADER = "X-User-Role";

    /**
     * Get current HTTP request
     * @return HttpServletRequest or null if not in web context
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * Get current user role from request headers
     * @return user role or null if not present
     */
    public String getCurrentUserRole() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.warn("No HTTP request context available");
            return null;
        }
        return request.getHeader(ROLE_HEADER);
    }

    /**
     * Check if current user has ADMIN role
     * @return true if user has ADMIN role
     */
    public boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ADMIN".equals(role);
    }

    /**
     * Check if current user has PRODUCT_MANAGER role
     * @return true if user has PRODUCT_MANAGER role
     */
    public boolean isProductManager() {
        String role = getCurrentUserRole();
        return "PRODUCT_MANAGER".equals(role);
    }

    /**
     * Check if current user is authenticated
     * @return true if user has any role
     */
    public boolean isAuthenticated() {
        return getCurrentUserRole() != null;
    }

    /**
     * Check if current user can manage products (ADMIN or PRODUCT_MANAGER)
     * @return true if user has ADMIN or PRODUCT_MANAGER role
     */
    public boolean canManageProducts() {
        return isAdmin() || isProductManager();
    }

    /**
     * Require ADMIN or PRODUCT_MANAGER role
     * @throws IllegalStateException if user doesn't have required role
     */
    public void requireProductManagement() {
        if (!canManageProducts()) {
            throw new IllegalStateException("Access denied. ADMIN or PRODUCT_MANAGER role required.");
        }
    }
}