-- ==============================================
-- Database: primavera (chap10)
-- OAuth2 Social Login Application
-- ==============================================

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255),
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     INT DEFAULT 1,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_USERS_EMAIL (EMAIL)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 권한 테이블 (Role 모델: id, type 필드만)
CREATE TABLE IF NOT EXISTS ROLES
(
    ID   BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE INT NOT NULL
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

-- 소셜 로그인 연결 테이블
CREATE TABLE IF NOT EXISTS USER_CONNECTION
(
    ID           BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL        VARCHAR(100)         NOT NULL,
    PROVIDER     INT                  NOT NULL,
    PROVIDER_ID  VARCHAR(100)         NOT NULL,
    DISPLAY_NAME VARCHAR(100),
    PROFILE_URL  VARCHAR(500),
    IMAGE_URL    VARCHAR(500),
    ACCESS_TOKEN VARCHAR(1000),
    EXPIRE_TIME  BIGINT DEFAULT 0,
    FOREIGN KEY (EMAIL) REFERENCES USERS (EMAIL) ON DELETE CASCADE,
    UNIQUE KEY UNIQUE_PROVIDER_USER (PROVIDER, PROVIDER_ID),
    INDEX IDX_USER_CONNECTION_EMAIL (EMAIL)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 기본 권한 데이터 (RoleType enum: 1=ADMINISTRATOR, 2=MANAGER, 3=USER)
INSERT INTO ROLES (ID, TYPE)
VALUES (1, 1),  -- ADMINISTRATOR
       (2, 2),  -- MANAGER  
       (3, 3)   -- USER
ON DUPLICATE KEY UPDATE TYPE = VALUES(TYPE);

-- 기본 사용자 데이터
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT, UPDATED_AT)
VALUES (1, 'admin@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Administrator', 1, NOW(), NOW()),
       (2, 'manager@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Manager', 1, NOW(), NOW()),
       (3, 'user@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'User', 1, NOW(), NOW()),
       (4, 'social@primavera.com', NULL, 'Social User', 1, NOW(), NOW()),
       (5, 'genius@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Genius Choi', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 사용자-권한 매핑
INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), (1, 3),  -- admin has all roles
       (2, 2), (2, 3),           -- manager has manager and user roles
       (3, 3),                   -- user has user role
       (4, 3),                   -- social user has user role
       (5, 1), (5, 2), (5, 3)    -- genius has all roles
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

-- 소셜 로그인 연결 데이터 (테스트용)
INSERT INTO USER_CONNECTION (EMAIL, PROVIDER, PROVIDER_ID, DISPLAY_NAME, PROFILE_URL, IMAGE_URL, ACCESS_TOKEN, EXPIRE_TIME)
VALUES ('social@primavera.com', 2, 'google123456', 'Google User', 'https://plus.google.com/+googleuser', 'https://lh3.googleusercontent.com/avatar.jpg', 'google_access_token_example', 1234567890),
       ('genius@primavera.com', 3, 'github789', 'Genius GitHub', 'https://github.com/genius', 'https://avatars.githubusercontent.com/genius', 'github_access_token_example', 1234567890)
ON DUPLICATE KEY UPDATE PROVIDER_ID = VALUES(PROVIDER_ID);
