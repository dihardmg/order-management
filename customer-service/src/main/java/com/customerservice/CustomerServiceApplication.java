package com.customerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application class for Customer Service
 * Entry point for the Customer Management microservice
 */
@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
        System.out.println("====================================");
        System.out.println("CUSTOMER SERVICE STARTED SUCCESSFULLY");
        System.out.println("Port: 8083");
        System.out.println("Database: customerdb (PostgreSQL 17)");
        System.out.println("====================================");
    }
}
