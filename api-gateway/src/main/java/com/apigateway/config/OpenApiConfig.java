package com.apigateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway - Order Management System")
                        .version("1.0")
                        .description("""
                                ## API Gateway Documentation
                                
                                **Spring Cloud Gateway | Spring Boot 3.5.13 | Java 21**
                                
                                Central API Gateway for the Order Management Microservices system.
                                
                                ### 🔓 Public Endpoints (No Auth Required)
                                
                                **Authentication & User Management:**
                                - `POST /api/auth/login` - User login
                                - `POST /api/auth/register` - User registration
                                
                                **Product Catalog (Read-Only):**
                                - `GET /api/products/**` - Product listing
                                - `GET /api/categories/**` - Category listing
                                
                                **System:**
                                - `GET /actuator/health` - Health check
                                
                                ---
                                
                                ### 🔐 Protected Endpoints (JWT Required)
                                
                                **Order Management:**
                                - `POST /api/orders` - Create order
                                - `GET /api/orders/**` - View orders
                                - `PATCH /api/orders/**` - Update order
                                
                                **Customer Management:**
                                - `GET /api/customers/**` - View customers
                                - `POST /api/customers` - Create customer
                                - `PUT /api/customers/**` - Update customer
                                - `DELETE /api/customers/**` - Delete customer
                                
                                **Product & Category Management (Write Operations):**
                                - `POST /api/products` - Create product (ADMIN/PRODUCT_MANAGER)
                                - `PUT /api/products/**` - Update product (ADMIN/PRODUCT_MANAGER)
                                - `DELETE /api/products/**` - Delete product (ADMIN/PRODUCT_MANAGER)
                                - `POST /api/categories` - Create category (ADMIN/PRODUCT_MANAGER)
                                - `PUT /api/categories/**` - Update category (ADMIN/PRODUCT_MANAGER)
                                - `DELETE /api/categories/**` - Delete category (ADMIN/PRODUCT_MANAGER)
                                - `PATCH /api/products/**/stock` - Update stock (USER/ADMIN)
                                
                                ### 🔑 How to Get JWT Token
                                
                                **1. Register Account:**
                                ```bash
                                POST /api/auth/register
                                {
                                  "username": "fany",
                                  "email": "fany@email.com",
                                  "password": "password",
                                  "fullName": "Fany"
                                }
                                ```
                                
                                **2.1 Login USER:**
                                ```bash
                                POST /api/auth/login
                                {
                                  "email": "fany@email.com",
                                  "password": "password"
                                }
                        
                                ```
                                
                                 **2.2 Login ADMIN:**
                                 
                                ```bash
                                POST /api/auth/login
                                {
                                  "email": "admin@system.com",
                                  "password": "password"
                                }
                                
                                ```
                              
                                **3. Copy token from response**
                                
                                **4. Use token in Swagger UI:**
                                - Click 🔓 **Authorize** button
                                - Enter: `Bearer <your-token>`
                                - Click **Authorize**
                                """)
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@apigateway.com")
                                .url("https://github.com/order-management"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token authentication. Get token from /api/auth/login")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("http://api-gateway:8080")
                                .description("Production Server")
                ));
    }

    /**
     * Public APIs - No authentication required
     */
    @Bean
    public GroupedOpenApi publicApis() {
        return GroupedOpenApi.builder()
                .group("public-no-auth")
                .pathsToMatch(
                        "/api/auth/**",
                        "/actuator/**",
                        "/api/products/**",
                        "/api/categories/**"
                )
                .build();
    }

    /**
     * Protected APIs - JWT authentication required
     */
    @Bean
    public GroupedOpenApi protectedApis() {
        return GroupedOpenApi.builder()
                .group("protected-jwt-required")
                .pathsToMatch(
                        "/api/orders/**",
                        "/api/customers/**"
                )
                .build();
    }
}
