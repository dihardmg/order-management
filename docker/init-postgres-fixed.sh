#!/bin/bash
set -e

# Function to create database
function create_database() {
    local database=$1
    echo "Creating database: $database"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d postgres <<-EOSQL
        CREATE DATABASE $database;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
}

# Create additional databases
if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Creating multiple databases..."
    for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
        create_database $db
    done
    echo "Multiple databases created successfully!"
fi

# userdb is already created via POSTGRES_MULTIPLE_DATABASES, so we skip creating it again
echo "userdb already created via POSTGRES_MULTIPLE_DATABASES, skipping..."

# Create schemas and tables for orderdb
echo "Setting up orderdb schema..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "orderdb" <<-EOSQL
    -- Orders table
    CREATE TABLE IF NOT EXISTS orders (
        id BIGSERIAL PRIMARY KEY,
        customer_id BIGINT NOT NULL,
        order_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        status VARCHAR(50) NOT NULL,
        total_amount DECIMAL(15,2) NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'))
    );

    -- Order items table
    CREATE TABLE IF NOT EXISTS order_items (
        id BIGSERIAL PRIMARY KEY,
        order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
        product_id BIGINT NOT NULL,
        quantity INTEGER NOT NULL CHECK (quantity > 0),
        price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

    -- Indexes
    CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
    CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(order_date);
    CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
    CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
    CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);

    -- Insert sample data - Indonesian customers orders
    INSERT INTO orders (customer_id, order_date, status, total_amount) VALUES
        (1, CURRENT_TIMESTAMP - INTERVAL '1 day', 'DELIVERED', 16749000),
        (2, CURRENT_TIMESTAMP - INTERVAL '2 days', 'PROCESSING', 25249000),
        (3, CURRENT_TIMESTAMP, 'PENDING', 1250000),
        (4, CURRENT_TIMESTAMP - INTERVAL '3 days', 'DELIVERED', 16749000),
        (5, CURRENT_TIMESTAMP - INTERVAL '5 hours', 'SHIPPED', 475000);

    INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
        (1, 1, 1, 15999000),
        (1, 6, 3, 125000),
        (2, 2, 1, 24999000),
        (3, 4, 1, 350000),
        (3, 5, 2, 450000),
        (4, 1, 1, 15999000),
        (4, 3, 5, 150000),
        (5, 7, 2, 175000),
        (5, 6, 1, 125000);
EOSQL

# Create schemas and tables for productdb
echo "Setting up productdb schema..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "productdb" <<-EOSQL
    -- Categories table
    CREATE TABLE IF NOT EXISTS categories (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE,
        description TEXT,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

    -- Products table
    CREATE TABLE IF NOT EXISTS products (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
        stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
        category_id BIGINT REFERENCES categories(id),
        sku VARCHAR(50) UNIQUE,
        is_active BOOLEAN DEFAULT true,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

    -- Indexes
    CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);
    CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
    CREATE INDEX IF NOT EXISTS idx_products_active ON products(is_active);

    -- Insert sample data - Indonesian products
    INSERT INTO categories (name, description) VALUES
        ('Elektronik', 'Perangkat elektronik dan aksesoris komputer'),
        ('Fashion', 'Pakaian dan fashion item untuk pria dan wanita'),
        ('Buku', 'Buku pendidikan dan publikasi'),
        ('Kesehatan', 'Produk kesehatan dan kecantikan'),
        ('Makanan', 'Produk makanan dan minuman');

    INSERT INTO products (name, description, price, stock, category_id, sku, is_active) VALUES
        ('Laptop Gaming ASUS ROG', 'Laptop gaming Intel Core i7, RAM 16GB, SSD 512GB', 15999000, 25, 1, 'LAPTOP-ASUS-001', true),
        ('iPhone 15 Pro Max', 'Smartphone Apple 256GB, Titanium Blue', 24999000, 15, 1, 'IPHONE-15-PM-256', true),
        ('Kaos Polos Cotton', 'Kaos katun premium 100% cotton, tersedia berbagai warna', 150000, 500, 2, 'KAOS-POLOS-001', true),
        ('Kemeja Batik Pria', 'Kemeja batik premium, bahan katun primisima', 350000, 100, 2, 'BATIK-KEMEJA-001', true),
        ('Buku Panduan Java 21', 'Buku pemrograman Java 21 untuk pemula dan mahir', 450000, 75, 3, 'BUKU-JAVA-21', true),
        ('Vitamin C 1000mg', 'Suplemen vitamin C untuk daya tahan tubuh', 125000, 200, 4, 'VITAMIN-C-1000', true),
        ('Kopi Arabika Premium', 'Kopi arabika specialty 250gr, roasted to order', 175000, 150, 5, 'KOPI-ARABIKA-250', true);
EOSQL

# Create schemas and tables for customerdb
echo "Setting up customerdb schema..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "customerdb" <<-EOSQL
    -- Customers table
    CREATE TABLE IF NOT EXISTS customers (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        phone VARCHAR(20),
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

    -- Customer addresses table
    CREATE TABLE IF NOT EXISTS customer_addresses (
        id BIGSERIAL PRIMARY KEY,
        customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
        address TEXT NOT NULL,
        city VARCHAR(100) NOT NULL,
        postal_code VARCHAR(10),
        is_default BOOLEAN DEFAULT false,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

    -- Indexes
    CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);
    CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer_id ON customer_addresses(customer_id);

    -- Insert sample data - Indonesian customers with Jakarta addresses
    INSERT INTO customers (name, email, phone) VALUES
        ('Budi Santoso', 'budi.santoso@email.com', '+6281234567890'),
        ('Siti Rahayu', 'siti.rahayu@email.com', '+6282345678901'),
        ('Ahmad Wijaya', 'ahmad.wijaya@email.com', '+6283456789012'),
        ('Dewi Lestari', 'dewi.lestari@email.com', '+6284567890123'),
        ('Agus Pratama', 'agus.pratama@email.com', '+6285678901234'),
        ('Rina Wati', 'rina.wati@email.com', '+6286789012345'),
        ('Doni Kurniawan', 'doni.kurniawan@email.com', '+6287890123456'),
        ('Maya Sari', 'maya.sari@email.com', '+6288901234567'),
        ('Feri Setiawan', 'feri.setiawan@email.com', '+6289012345678'),
        ('Linda Permata', 'linda.permata@email.com', '+6280123456789');

    INSERT INTO customer_addresses (customer_id, address, city, postal_code, is_default) VALUES
        -- Jakarta Selatan addresses
        (1, 'Jl. Fatmawati No. 123, Kebayoran Baru', 'Jakarta Selatan', '12150', true),
        (2, 'Jl. Senopati No. 45, Kebayoran Baru', 'Jakarta Selatan', '12190', true),
        (3, 'Jl. Kemang Raya No. 78, Kemang', 'Jakarta Selatan', '12730', true),

        -- Jakarta Pusat addresses
        (4, 'Jl. Sudirman No. 234, Senayan', 'Jakarta Pusat', '10270', true),
        (5, 'Jl. Thamrin No. 56, Menteng', 'Jakarta Pusat', '10350', true),
        (6, 'Jl. Gatot Subroto No. 89, Kuningan', 'Jakarta Pusat', '12950', true),

        -- Jakarta Barat addresses
        (7, 'Jl. S. Parman No. 12, Tomang', 'Jakarta Barat', '11440', true),
        (8, 'Jl. Daan Mogot No. 67, Kalideres', 'Jakarta Barat', '11840', true),

        -- Jakarta Timur addresses
        (9, 'Jl. Matraman Raya No. 34, Matraman', 'Jakarta Timur', '13140', true),
        (10, 'Jl. Buaran Raya No. 90, Duren Sawit', 'Jakarta Timur', '13440', true);

    -- Insert additional addresses for some customers (multiple address scenario)
    INSERT INTO customer_addresses (customer_id, address, city, postal_code, is_default) VALUES
        (1, 'Jl. Blok A No. 15, Cipete', 'Jakarta Selatan', '12410', false),
        (4, 'Jl. Tanah Abang I No. 23, Tanah Abang', 'Jakarta Pusat', '10160', false),
        (7, 'Jl. Puri Indah Raya No. 8, Kembangan', 'Jakarta Barat', '11610', false);
EOSQL

# Create schemas and tables for userdb (API Gateway authentication)
echo "Setting up userdb schema for API Gateway..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "userdb" <<'EOSQL'
    -- Users table for authentication
    CREATE TABLE IF NOT EXISTS users (
        id BIGSERIAL PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        email VARCHAR(100) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        full_name VARCHAR(100) NOT NULL,
        role VARCHAR(20) NOT NULL DEFAULT 'USER',
        enabled BOOLEAN DEFAULT true,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

    -- Indexes
    CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
    CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

    -- Insert sample users (password: 'password' encoded with BCrypt)
    INSERT INTO users (username, email, password, full_name, role, enabled) VALUES
        ('budi.santoso', 'budi.santoso@email.com', '$2a$10$Il71O934CJ0yrlocsqBP5eHuOI0Q4pl9AQYEzqUtzV/BYatQ3uyZm', 'Budi Santoso', 'USER', true),
        ('siti.rahayu', 'siti.rahayu@email.com', '$2a$10$Il71O934CJ0yrlocsqBP5eHuOI0Q4pl9AQYEzqUtzV/BYatQ3uyZm', 'Siti Rahayu', 'USER', true),
        ('admin', 'admin@system.com', '$2a$10$Il71O934CJ0yrlocsqBP5eHuOI0Q4pl9AQYEzqUtzV/BYatQ3uyZm', 'System Administrator', 'ADMIN', true);
EOSQL

echo "All databases created and initialized successfully!"
