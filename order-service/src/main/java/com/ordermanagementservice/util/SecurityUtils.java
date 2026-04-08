package com.ordermanagementservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Security utility class for Order Service
 * Extracts user role and ID from headers set by API Gateway
 */
@Component
@Slf4j
public class SecurityUtils {

    private static final String ROLE_HEADER = "X-User-Role";
    private static final String USER_ID_HEADER = "X-User-Id";

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
     * Get current user ID from request headers
     * @return user ID or null if not present
     */
    public Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.warn("No HTTP request context available");
            return null;
        }
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader == null) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return null;
        }
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
     * Check if current user has USER role
     * @return true if user has USER role
     */
    public boolean isUser() {
        String role = getCurrentUserRole();
        return "USER".equals(role);
    }

    /**
     * Check if current user is authenticated
     * @return true if user has any role
     */
    public boolean isAuthenticated() {
        return getCurrentUserRole() != null;
    }

    /**
     * Check if current user can access order (admin or owner)
     * @param orderCustomerId the customer ID of the order
     * @return true if user is admin or the order owner
     */
    public boolean canAccessOrder(Long orderCustomerId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        // Admin can access all orders, users can only access their own
        return isAdmin() || currentUserId.equals(orderCustomerId);
    }

    /**
     * Require ADMIN role
     * @throws IllegalStateException if user doesn't have ADMIN role
     */
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new IllegalStateException("Access denied. ADMIN role required.");
        }
    }

    /**
     * Require authentication
     * @throws IllegalStateException if user is not authenticated
     */
    public void requireAuthentication() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Access denied. Authentication required.");
        }
    }
}
