CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255),
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     INT      DEFAULT 1,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_USERS_EMAIL (EMAIL)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 표준 사용자 테이블 데이터 (chap04는 ROLES/USER_ROLES 테이블 없음)
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT, UPDATED_AT)
VALUES (1, 'genius@primavera.com', '{noop}test', 'Genius', 1, NOW(), NOW()),
       (2, 'admin@primavera.com', '{noop}test', 'Admin', 1, NOW(), NOW()),
       (3, 'user@primavera.com', '{noop}test', 'User', 1, NOW(), NOW()),
       (4, 'son@primavera.com', '{noop}test', 'Son', 1, NOW(), NOW()),
       (5, 'messi@primavera.com', '{noop}test', 'Messi', 1, NOW(), NOW()),
       (6, 'ronaldo@primavera.com', '{noop}test', 'Ronaldo', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);