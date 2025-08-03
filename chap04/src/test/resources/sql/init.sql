-- ==============================================
-- Chapter 04 - Test Data
-- Database: primavera_test
-- ==============================================

CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     INT      DEFAULT 1,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_EMAIL (EMAIL),
    INDEX IDX_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터 (UserDaoTest에서 초기 4명, saveUser()로 1명 추가해서 총 5명)
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'genius@primavera.com', 'password', 'Genius', 1),      -- PrimaveraServiceTest에서 필요
       (2, 'admin@primavera.com', 'password', 'Administrator', 1),
       (3, 'user@primavera.com', 'password', 'User', 1),
       (4, 'test@primavera.com', 'password', 'TestUser', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);