-- ==============================================
-- Database: primavera
-- ==============================================

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     INT DEFAULT 1,
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_EMAIL (EMAIL),
    INDEX IDX_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 권한 테이블
CREATE TABLE IF NOT EXISTS ROLES
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME        VARCHAR(50) UNIQUE NOT NULL,
    DESCRIPTION VARCHAR(255),
    TYPE        INT                NOT NULL,
    CREATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자-권한 연결 테이블
CREATE TABLE IF NOT EXISTS USER_ROLES
(
    ID      BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID BIGINT NOT NULL,
    ROLE_ID BIGINT NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    FOREIGN KEY (ROLE_ID) REFERENCES ROLES (ID) ON DELETE CASCADE,
    UNIQUE KEY UNIQUE_USER_ROLE (USER_ID, ROLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 기본 권한 데이터
INSERT INTO ROLES (ID, NAME, DESCRIPTION, TYPE)
VALUES (1, 'ROLE_ADMINISTRATOR', '최고 관리자', 1),
       (2, 'ROLE_MANAGER', '관리자', 2),
       (3, 'ROLE_USER', '일반 사용자', 3)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 기본 사용자 데이터
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'admin@primavera.com', '{noop}admin123', 'Administrator', 1),
       (2, 'manager@primavera.com', '{noop}manager123', 'Manager', 1),
       (3, 'user@primavera.com', '{noop}user123', 'User', 1),
       (4, 'genius@primavera.com', '{noop}test', 'Genius', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 사용자-권한 매핑
INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), (1, 3), -- admin has all roles
       (2, 2), (2, 3),          -- manager has manager and user roles
       (3, 3),                  -- user has user role
       (4, 1), (4, 2), (4, 3)   -- genius has all roles
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);
