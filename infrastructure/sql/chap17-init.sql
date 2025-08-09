-- ==============================================
-- chap17 Multi-Module Architecture Database
-- Batch Processing + Streaming + CDC Support
-- ==============================================

-- Product Table for Batch Processing
CREATE TABLE IF NOT EXISTS PRODUCTS
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME        VARCHAR(255) NOT NULL,
    DESCRIPTION TEXT,
    PRICE       DECIMAL(10, 2) NOT NULL,
    CATEGORY    VARCHAR(100) NOT NULL,
    SKU         VARCHAR(50) UNIQUE NOT NULL,
    STOCK       INT DEFAULT 0,
    STATUS      VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_CATEGORY (CATEGORY),
    INDEX IDX_STATUS (STATUS),
    INDEX IDX_SKU (SKU)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Sample Data
INSERT INTO PRODUCTS (ID, NAME, DESCRIPTION, PRICE, CATEGORY, SKU, STOCK, STATUS)
VALUES (1, 'Spring Boot Guide', 'Comprehensive guide to Spring Boot development', 49.99, 'BOOKS', 'BOOK-001', 100, 'ACTIVE'),
       (2, 'Reactive Programming', 'Learn reactive programming with Spring WebFlux', 39.99, 'BOOKS', 'BOOK-002', 50, 'ACTIVE'),
       (3, 'Microservices Patterns', 'Design patterns for microservices architecture', 59.99, 'BOOKS', 'BOOK-003', 75, 'ACTIVE'),
       (4, 'Docker Container', 'Containerization with Docker', 45.99, 'BOOKS', 'BOOK-004', 80, 'ACTIVE'),
       (5, 'Kubernetes Guide', 'Container orchestration with Kubernetes', 69.99, 'BOOKS', 'BOOK-005', 60, 'ACTIVE')
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);