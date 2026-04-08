package com.ordermanagementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application class for Order Service
 * Entry point for the Order Management microservice
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("=================================");
        System.out.println("ORDER SERVICE STARTED SUCCESSFULLY");
        System.out.println("Port: 8081");
        System.out.println("Database: orderdb (PostgreSQL 17)");
        System.out.println("===================================");
    }
}
