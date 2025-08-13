-- ==============================================
-- Database: primavera - Microservices
-- ==============================================

-- Account Service Schema
CREATE SCHEMA IF NOT EXISTS account_service;
USE account_service;

CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100) UNIQUE NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    password   VARCHAR(255)        NOT NULL,
    status     VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_USERNAME (username),
    INDEX IDX_EMAIL (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Order Service Schema
CREATE SCHEMA IF NOT EXISTS order_service;
USE order_service;

CREATE TABLE IF NOT EXISTS orders
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    order_no    VARCHAR(50) UNIQUE NOT NULL,
    total_price DECIMAL(10, 2)     NOT NULL,
    status      VARCHAR(20) DEFAULT 'PENDING',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_USER_ID (user_id),
    INDEX IDX_ORDER_NO (order_no),
    INDEX IDX_STATUS (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_items
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   INT            NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    INDEX IDX_ORDER_ID (order_id),
    INDEX IDX_PRODUCT_ID (product_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Product Service Schema
CREATE SCHEMA IF NOT EXISTS product_service;
USE product_service;

CREATE TABLE IF NOT EXISTS products
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200)   NOT NULL,
    description TEXT,
    price       DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    category    VARCHAR(50),
    status      VARCHAR(20)             DEFAULT 'AVAILABLE',
    created_at  DATETIME                DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_NAME (name),
    INDEX IDX_CATEGORY (category),
    INDEX IDX_STATUS (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Insert sample data
USE account_service;
INSERT INTO users (username, email, password, status)
VALUES ('admin', 'admin@primavera.com', '{noop}admin123', 'ACTIVE'),
       ('user1', 'user1@primavera.com', '{noop}user123', 'ACTIVE'),
       ('user2', 'user2@primavera.com', '{noop}user123', 'ACTIVE');

USE product_service;
INSERT INTO products (name, description, price, stock, category, status)
VALUES ('Spring Boot Book', 'Complete guide to Spring Boot', 45000, 100, 'BOOK', 'AVAILABLE'),
       ('Java Programming', 'Java programming fundamentals', 38000, 50, 'BOOK', 'AVAILABLE'),
       ('Cloud Architecture', 'Microservices and cloud patterns', 52000, 30, 'BOOK', 'AVAILABLE');

-- Grant permissions
GRANT ALL PRIVILEGES ON account_service.* TO 'primavera'@'%';
GRANT ALL PRIVILEGES ON order_service.* TO 'primavera'@'%';
GRANT ALL PRIVILEGES ON product_service.* TO 'primavera'@'%';
FLUSH PRIVILEGES;