-- ==============================================
-- Primavera Chapter 01-05 - Basic Environment Database
-- Spring Boot 기초, 설정, MVC, AOP, 데이터베이스 기본
-- MariaDB 11.4.7 - 기본 학습용 구조
-- ==============================================

-- 기본 데이터베이스 설정
ALTER DATABASE primavera CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==============================================
-- 기본 학습용 데이터베이스 생성
-- ==============================================

-- 1. 기본 학습용 데이터베이스
CREATE DATABASE IF NOT EXISTS primavera_basic CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==============================================
-- 권한 설정
-- ==============================================

GRANT ALL PRIVILEGES ON primavera.* TO 'primavera'@'%' IDENTIFIED BY 'primavera';
GRANT ALL PRIVILEGES ON primavera_basic.* TO 'primavera'@'%' IDENTIFIED BY 'primavera';

FLUSH PRIVILEGES;

-- ==============================================
-- 기본 데이터베이스 테이블 (primavera)
-- Chapter 01-03용 기본 테이블
-- ==============================================

USE primavera;

-- 기본 사용자 테이블 (chap01-03용)
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 기본 역할 테이블
CREATE TABLE IF NOT EXISTS ROLES
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    ROLE_NAME   VARCHAR(50) UNIQUE NOT NULL,
    DESCRIPTION VARCHAR(200),
    CREATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 역할 매핑 테이블
CREATE TABLE IF NOT EXISTS USER_ROLES
(
    ID      BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID BIGINT NOT NULL,
    ROLE_ID BIGINT NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    FOREIGN KEY (ROLE_ID) REFERENCES ROLES (ID) ON DELETE CASCADE,
    UNIQUE KEY UK_USER_ROLE (USER_ID, ROLE_ID)
);

-- ==============================================
-- 확장 학습용 데이터베이스 (primavera_basic)
-- Chapter 04-05용 데이터 접근 학습 테이블
-- ==============================================

USE primavera_basic;

-- 학습용 사용자 테이블 (확장 버전)
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    FIRST_NAME VARCHAR(50),
    LAST_NAME  VARCHAR(50),
    PHONE      VARCHAR(20),
    STATUS     VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CREATED_BY BIGINT,
    UPDATED_BY BIGINT,
    
    INDEX IDX_USERS_EMAIL (EMAIL),
    INDEX IDX_USERS_STATUS (STATUS),
    INDEX IDX_USERS_CREATED_AT (CREATED_AT)
);

-- 학습용 제품 테이블
CREATE TABLE IF NOT EXISTS PRODUCTS
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME        VARCHAR(100) NOT NULL,
    DESCRIPTION TEXT,
    PRICE       DECIMAL(10, 2) NOT NULL,
    CATEGORY    VARCHAR(50),
    STATUS      VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_PRODUCTS_CATEGORY (CATEGORY),
    INDEX IDX_PRODUCTS_STATUS (STATUS),
    INDEX IDX_PRODUCTS_PRICE (PRICE)
);

-- 학습용 주문 테이블
CREATE TABLE IF NOT EXISTS ORDERS
(
    ID           BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID      BIGINT         NOT NULL,
    ORDER_NUMBER VARCHAR(50)    NOT NULL UNIQUE,
    TOTAL_AMOUNT DECIMAL(12, 2) NOT NULL,
    STATUS       VARCHAR(20) DEFAULT 'PENDING',
    ORDER_DATE   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    CREATED_AT   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (USER_ID) REFERENCES USERS (ID),
    INDEX IDX_ORDERS_USER_ID (USER_ID),
    INDEX IDX_ORDERS_STATUS (STATUS),
    INDEX IDX_ORDERS_ORDER_DATE (ORDER_DATE)
);

-- 학습용 주문 상세 테이블
CREATE TABLE IF NOT EXISTS ORDER_ITEMS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ORDER_ID   BIGINT         NOT NULL,
    PRODUCT_ID BIGINT         NOT NULL,
    QUANTITY   INT            NOT NULL,
    UNIT_PRICE DECIMAL(10, 2) NOT NULL,
    TOTAL_PRICE DECIMAL(12, 2) NOT NULL,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (ORDER_ID) REFERENCES ORDERS (ID) ON DELETE CASCADE,
    FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCTS (ID),
    INDEX IDX_ORDER_ITEMS_ORDER_ID (ORDER_ID),
    INDEX IDX_ORDER_ITEMS_PRODUCT_ID (PRODUCT_ID)
);

-- ==============================================
-- 초기 데이터 삽입
-- ==============================================

-- 기본 데이터베이스 초기 데이터
USE primavera;

-- 기본 역할 데이터
INSERT IGNORE INTO ROLES (ROLE_NAME, DESCRIPTION) VALUES 
('ROLE_USER', '일반 사용자'),
('ROLE_ADMIN', '관리자'),
('ROLE_MANAGER', '매니저');

-- 기본 사용자 데이터
INSERT IGNORE INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES 
('admin@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin'),
('user@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'User'),
('manager@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Manager');

-- 사용자 역할 매핑
INSERT IGNORE INTO USER_ROLES (USER_ID, ROLE_ID) VALUES 
(1, 2), -- Admin
(2, 1), -- User
(3, 3); -- Manager

-- 확장 데이터베이스 초기 데이터
USE primavera_basic;

-- 학습용 사용자 데이터
INSERT IGNORE INTO USERS (EMAIL, PASSWORD, NICKNAME, FIRST_NAME, LAST_NAME, PHONE) VALUES 
('john.doe@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'johndoe', 'John', 'Doe', '010-1234-5678'),
('jane.smith@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'janesmith', 'Jane', 'Smith', '010-2345-6789'),
('bob.wilson@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'bobwilson', 'Bob', 'Wilson', '010-3456-7890');

-- 학습용 제품 데이터
INSERT IGNORE INTO PRODUCTS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES 
('Spring Boot 완벽가이드', 'Spring Boot 학습을 위한 완벽한 가이드북', 35000.00, 'BOOK'),
('Java 프로그래밍', 'Java 기초부터 고급까지 학습서', 28000.00, 'BOOK'),
('웹 개발 실전', '실전 웹 개발 프로젝트 가이드', 42000.00, 'BOOK'),
('데이터베이스 설계', '효율적인 데이터베이스 설계 방법론', 38000.00, 'BOOK'),
('마이크로서비스 아키텍처', '현대적인 마이크로서비스 구축 가이드', 45000.00, 'BOOK');

-- 학습용 주문 데이터
INSERT IGNORE INTO ORDERS (USER_ID, ORDER_NUMBER, TOTAL_AMOUNT, STATUS) VALUES 
(1, 'ORD-2024-001', 63000.00, 'COMPLETED'),
(2, 'ORD-2024-002', 42000.00, 'COMPLETED'),
(3, 'ORD-2024-003', 83000.00, 'PENDING');

-- 학습용 주문 상세 데이터
INSERT IGNORE INTO ORDER_ITEMS (ORDER_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE, TOTAL_PRICE) VALUES 
(1, 1, 1, 35000.00, 35000.00),
(1, 2, 1, 28000.00, 28000.00),
(2, 3, 1, 42000.00, 42000.00),
(3, 4, 1, 38000.00, 38000.00),
(3, 5, 1, 45000.00, 45000.00);

-- ==============================================
-- 종료 메시지
-- ==============================================

SELECT 'Primavera Basic Environment Database Initialization Completed!' as STATUS;