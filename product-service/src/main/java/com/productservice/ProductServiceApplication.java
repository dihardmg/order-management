package com.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application class for Product Service
 * Entry point for the Product Catalog microservice
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
        System.out.println("===================================");
        System.out.println("PRODUCT SERVICE STARTED SUCCESSFULLY");
        System.out.println("Port: 8082");
        System.out.println("Database: productdb (PostgreSQL 17)");
        System.out.println("===================================");
    }
}
