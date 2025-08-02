-- ==============================================
-- Chapter 02 - Configuration and Environment Test Data
-- Uses primavera_test database (TestContainers)
-- Focus: Spring Boot configuration, properties, profiles
-- ==============================================

-- 기본 사용자 테이블 (Configuration 테스트용)
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_EMAIL (EMAIL),
    INDEX IDX_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 설정 테스트용 환경 설정 테이블
CREATE TABLE IF NOT EXISTS APP_CONFIGURATIONS
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    CONFIG_KEY  VARCHAR(100) UNIQUE NOT NULL,
    CONFIG_VALUE TEXT,
    DESCRIPTION VARCHAR(255),
    ACTIVE      BOOLEAN DEFAULT TRUE,
    CREATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터 - Chapter 02: Configuration & Environment
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'config@primavera.com', '{noop}test', 'ConfigTest', 'ACTIVE'),
       (2, 'admin@primavera.com', '{noop}test', 'AdminTest', 'ACTIVE')
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO APP_CONFIGURATIONS (CONFIG_KEY, CONFIG_VALUE, DESCRIPTION, ACTIVE)
VALUES ('test.environment', 'local', 'Test environment setting', TRUE),
       ('test.feature.enabled', 'true', 'Feature toggle for testing', TRUE),
       ('test.max.connections', '10', 'Maximum connections for test', TRUE)
ON DUPLICATE KEY UPDATE CONFIG_KEY = VALUES(CONFIG_KEY);