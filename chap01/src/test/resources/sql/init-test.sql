-- ==============================================
-- Primavera Chapter 01 - Test Environment Database
-- Spring Boot 기본 구조 학습용 테스트 환경
-- TestContainers MariaDB 11.4.7
-- ==============================================

-- 기본 사용자 테이블 (chap01 테스트용)
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

-- 테스트 데이터
INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES 
('test@example.com', 'password', 'TestUser'),
('admin@example.com', 'admin123', 'AdminUser');

SELECT 'Chapter 01 Test Database Initialized!' as STATUS;