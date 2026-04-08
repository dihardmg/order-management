package com.customerservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for security-related operations
 * Extracts user information from HTTP headers set by API Gateway
 */
@Component
@Slf4j
public class SecurityUtils {

    private static final String ROLE_HEADER = "X-User-Role";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String EMAIL_HEADER = "X-User-Email";

    /**
     * Get the current user's role from request headers
     * @return user role or null if not authenticated
     */
    public String getCurrentUserRole() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return null;
            }
            return request.getHeader(ROLE_HEADER);
        } catch (Exception e) {
            log.warn("Failed to get user role from request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get the current user's ID from request headers
     * @return user ID or null if not authenticated
     */
    public Long getCurrentUserId() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return null;
            }
            String userIdHeader = request.getHeader(USER_ID_HEADER);
            return userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        } catch (Exception e) {
            log.warn("Failed to get user ID from request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get the current user's email from request headers
     * @return user email or null if not authenticated
     */
    public String getCurrentUserEmail() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return null;
            }
            return request.getHeader(EMAIL_HEADER);
        } catch (Exception e) {
            log.warn("Failed to get user email from request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if current user has ADMIN role
     * @return true if user is ADMIN, false otherwise
     */
    public boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ADMIN".equals(role);
    }

    /**
     * Check if current user is authenticated
     * @return true if user has valid authentication
     */
    public boolean isAuthenticated() {
        return getCurrentUserRole() != null;
    }

    /**
     * Get current HTTP request
     * @return HttpServletRequest or null if not in web context
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Validate that current user has ADMIN role
     * @throws IllegalStateException if user is not ADMIN
     */
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new IllegalStateException("Access denied. ADMIN role required.");
        }
    }

    /**
     * Validate that current user is authenticated
     * @throws IllegalStateException if user is not authenticated
     */
    public void requireAuthenticated() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Access denied. Authentication required.");
        }
    }
}
