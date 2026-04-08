# API Quick Reference Guide

## Base URLs
- **API Gateway**: `http://localhost:8080`
- **Order Service**: `http://localhost:8081`
- **Product Service**: `http://localhost:8082`
- **Customer Service**: `http://localhost:8083`
- **Postgres database browser**: `http://localhost:5050/browser/`

## Authentication

### Login & Get Token
```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email": "user@email.com", "password": "password"}'
```

### Use Token
```bash
# Save token to variable
TOKEN="your_jwt_token_here"

# Use in requests
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/orders
```

## Core Endpoints

### Auth Endpoints
```bash
# Register
POST /api/auth/register

# Login
POST /api/auth/login
```

###  Product Endpoints
```bash
# Get all products
GET /api/products

# Get product by ID
GET /api/products/{id}

# Search products
GET /api/products/search?name={keyword}

# Get products by category
GET /api/products/category?category={category}

# Get in-stock products
GET /api/products/in-stock

# Get price range
GET /api/products/price-range?min={min}&max={max}

# Create product (ADMIN)
POST /api/products

# Update product (ADMIN)
PUT /api/products/{id}

# Update stock (ADMIN/USER)
PATCH /api/products/{id}/stock?quantity={amount}

# Delete product (ADMIN)
DELETE /api/products/{id}
```

### Category Endpoints
```bash
# Get all categories
GET /api/categories

# Get category by ID
GET /api/categories/{id}

# Create category (ADMIN)
POST /api/categories

# Update category (ADMIN)
PUT /api/categories/{id}

# Delete category (ADMIN)
DELETE /api/categories/{id}
```

###  Order Endpoints
```bash
# Get all orders
GET /api/orders

# Get order by ID
GET /api/orders/{id}

# Get customer orders
GET /api/orders/customer/{customerId}

# Get orders by date range (NEW! LocalDate format)
GET /api/orders/date-range?startDate=2026-04-01&endDate=2026-04-30

# Get orders by status
GET /api/orders/status/{status}

# Create order
POST /api/orders

# Update order status
PATCH /api/orders/{id}

# Get order statistics
GET /api/orders/statistics?startDate=2026-04-01&endDate=2026-04-30
```

### Customer Endpoints
```bash
# Get all customers
GET /api/customers

# Get customer by ID
GET /api/customers/{id}

# Search customers
GET /api/customers/search?name={keyword}

# Create customer
POST /api/customers

# Update customer
PUT /api/customers/{id}

# Delete customer
DELETE /api/customers/{id}
```

###  Analytics Endpoints
```bash
# Daily analytics
GET /api/orders/analytics/daily?startDate=2026-04-01&endDate=2026-04-30

# Category analytics
GET /api/orders/analytics/category?startDate=2026-04-01&endDate=2026-04-30

# Revenue trends
GET /api/orders/analytics/revenue?startDate=2026-04-01&endDate=2026-04-30
```

## Quick Workflows

### 1. Create Order
```bash
TOKEN=$(curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email": "user@email.com", "password": "password"}' | jq -r '.token')

curl -X POST 'http://localhost:8080/api/orders' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 3, "quantity": 1}
    ]
  }'
```

### 2. Browse Products
```bash
# All products
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/products

# Search by name
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/products/search?name=Laptop"

# In-stock only
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/products/in-stock
```

### 3. Get Order Analytics
```bash
# Statistics
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/orders/statistics?startDate=2026-04-01&endDate=2026-04-30"

# Date range orders (NEW! LocalDate format)
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/orders/date-range?startDate=2026-04-01&endDate=2026-04-30"
```

## Error Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

## Date Formats
- **LocalDate**: `2026-04-01` (for date-range, statistics)
- **LocalDateTime**: `2026-04-01T10:30:00` (for specific timestamps)

## Default Users
- **Admin**: `admin@system.com` / `admin123`
- **User**: `fany@email.com` / `password`
