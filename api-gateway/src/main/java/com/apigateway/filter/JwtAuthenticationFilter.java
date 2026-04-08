package com.apigateway.filter;

import com.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/health",
            "/actuator/info",
            // Swagger UI and API docs - public access
            "/swagger-ui.html",
            "/swagger-ui",
            "/webjars/swagger-ui",
            "/api-docs",
            "/v3/api-docs"
            // Note: GET /api/products and /api/categories are public but still accept JWT tokens
            // The filter will process them if tokens are present, but won't block anonymous access
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // Skip authentication for excluded paths and Swagger UI
        if (isExcludedPath(path) || path.contains("swagger") || path.contains("webjars") || path.contains("api-docs")) {
            return chain.filter(exchange);
        }

        // Allow anonymous access for public GET endpoints
        boolean isPublicGetEndpoint = (path.startsWith("/api/products") || path.startsWith("/api/categories"))
                && "GET".equalsIgnoreCase(method);

        String authHeader = exchange.getRequest().getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        // For public GET endpoints, proceed without JWT if no auth header
        if (isPublicGetEndpoint && (authHeader == null || !authHeader.startsWith("Bearer "))) {
            return chain.filter(exchange);
        }

        // If auth header exists, validate it
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        log.debug("Processing token for path: {}, token preview: {}", path,
                 token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");

        if (!jwtUtil.validateToken(token)) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        try {
            // Extract user info from token
            Long userId = jwtUtil.extractUserId(token);
            String username = jwtUtil.extractUsername(token);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            // Add user info to request headers
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
            return unauthorized(exchange, "Error processing token");
        }
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}",
                java.time.LocalDateTime.now(),
                message
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // High priority filter
    }
}
