-- ==============================================
-- Chapter 08 - Security Filter Application
-- Database: primavera
-- ==============================================

-- 사용자 테이블 (매퍼에서 USERS 테이블명 사용)
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     INT DEFAULT 1,
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_USERS_EMAIL (EMAIL),
    INDEX IDX_USERS_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 권한 테이블 (매퍼에서 ROLES 테이블명 사용)
CREATE TABLE IF NOT EXISTS ROLES
(
    ID   BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE INT NOT NULL,
    INDEX IDX_ROLES_TYPE (TYPE)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자-권한 연결 테이블 (매퍼에서 USER_ROLES 테이블명 사용)
CREATE TABLE IF NOT EXISTS USER_ROLES
(
    ID      BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID BIGINT NOT NULL,
    ROLE_ID BIGINT NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    FOREIGN KEY (ROLE_ID) REFERENCES ROLES (ID) ON DELETE CASCADE,
    UNIQUE KEY UK_USER_ROLES (USER_ID, ROLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터: 권한 (RoleType enum 값)
INSERT INTO ROLES (ID, TYPE)
VALUES (1, 1), -- ADMINISTRATOR
       (2, 2), -- MANAGER  
       (3, 3)  -- USER
ON DUPLICATE KEY UPDATE TYPE = VALUES(TYPE);

-- 테스트 데이터: 사용자 (PrimaveraFilterTest에서 genius@gmail.com 사용)
-- Password: Secret0! (plain text로 저장 - 테스트용)
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'genius@gmail.com', 'Secret0!', 'Genius', 1),
       (2, 'admin@primavera.com', 'Secret0!', 'Administrator', 1),
       (3, 'user@primavera.com', 'Secret0!', 'User', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 테스트 데이터: 사용자-권한 매핑
INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 3), -- genius has USER role
       (2, 1), -- admin has ADMINISTRATOR role
       (3, 3)  -- user has USER role
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);