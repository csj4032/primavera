-- ==============================================
-- Database: primavera
-- ==============================================

-- 상품 테이블 (R2DBC용)
CREATE TABLE IF NOT EXISTS products
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    description TEXT,
    price      DECIMAL(10, 2) NOT NULL,
    category   VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_CATEGORY (category),
    INDEX IDX_PRICE (price)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터
INSERT INTO products (id, name, description, price, category, created_at, updated_at)
VALUES (1, 'Test Product', 'Test Product Description', 100000.00, 'Electronics', NOW(), NOW()),
       (2, 'Another Product', 'Another Product Description', 200000.00, 'Electronics', NOW(), NOW()),
       (3, 'Third Product', 'Third Product Description', 150000.00, 'Books', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);