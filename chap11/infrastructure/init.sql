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

-- OAuth2 사용자 연결 테이블
CREATE TABLE IF NOT EXISTS USER_CONNECTION
(
    ID           BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL        VARCHAR(100) NOT NULL,
    PROVIDER     INT          NOT NULL,
    PROVIDER_ID  VARCHAR(100) NOT NULL,
    DISPLAY_NAME VARCHAR(100),
    PROFILE_URL  VARCHAR(500),
    IMAGE_URL    VARCHAR(500),
    ACCESS_TOKEN VARCHAR(1000),
    EXPIRE_TIME  BIGINT,
    CREATED_AT   DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (EMAIL) REFERENCES USERS (EMAIL) ON DELETE CASCADE,
    UNIQUE KEY UNIQUE_PROVIDER_USER (PROVIDER, PROVIDER_ID),
    INDEX IDX_EMAIL (EMAIL),
    INDEX IDX_PROVIDER (PROVIDER)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Chapter 11 특화 테이블 - 게시글 테이블
CREATE TABLE IF NOT EXISTS POSTS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    SUBJECT    VARCHAR(200) NOT NULL,
    CONTENTS   LONGTEXT     NOT NULL,
    WRITER_ID  BIGINT       NOT NULL,
    STATUS     INT DEFAULT 1,
    VIEW_COUNT BIGINT      DEFAULT 0,
    LIKE_COUNT BIGINT      DEFAULT 0,
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (WRITER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    INDEX IDX_WRITER_ID (WRITER_ID),
    INDEX IDX_STATUS (STATUS),
    INDEX IDX_CREATED_AT (CREATED_AT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 댓글 테이블
CREATE TABLE IF NOT EXISTS COMMENTS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    POST_ID    BIGINT      NOT NULL,
    AUTHOR_ID  BIGINT      NOT NULL,
    CONTENT    TEXT        NOT NULL,
    STATUS     VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (POST_ID) REFERENCES POSTS (ID) ON DELETE CASCADE,
    FOREIGN KEY (AUTHOR_ID) REFERENCES USERS (ID),
    INDEX IDX_POST_ID (POST_ID),
    INDEX IDX_AUTHOR_ID (AUTHOR_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 첨부파일 테이블
CREATE TABLE IF NOT EXISTS ATTACHMENTS
(
    ID            BIGINT AUTO_INCREMENT PRIMARY KEY,
    POST_ID       BIGINT       NOT NULL,
    ORIGINAL_NAME VARCHAR(255) NOT NULL,
    STORED_NAME   VARCHAR(255) NOT NULL,
    FILE_PATH     VARCHAR(500) NOT NULL,
    FILE_SIZE     BIGINT       NOT NULL,
    CONTENT_TYPE  VARCHAR(100),
    CREATED_AT    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (POST_ID) REFERENCES POSTS (ID) ON DELETE CASCADE,
    INDEX IDX_POST_ID (POST_ID)
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

-- 기본 사용자 데이터 추가
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (5, 'board@primavera.com', '{noop}test', 'BoardAdmin', 1),
       (6, 'writer@primavera.com', '{noop}test', 'Writer', 1),
       (7, 'reader@primavera.com', '{noop}test', 'Reader', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 사용자-권한 매핑
INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), (1, 3), -- admin has all roles
       (2, 2), (2, 3),          -- manager has manager and user roles
       (3, 3),                  -- user has user role
       (4, 1), (4, 2), (4, 3),  -- genius has all roles
       (5, 1), (5, 2), (5, 3),  -- board admin has all roles
       (6, 2),                  -- writer has manager role
       (7, 3)                   -- reader has user role
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

-- 게시글 테스트 데이터
INSERT INTO POSTS (ID, SUBJECT, CONTENTS, WRITER_ID, STATUS, VIEW_COUNT, LIKE_COUNT)
VALUES (1, 'Board System Test Post 1', 'This is a test post for the board system. Content includes various features.', 5, 1, 100, 15),
       (2, 'Board System Test Post 2', 'Another test post with different content and features.', 6, 1, 75, 8),
       (3, 'Draft Post', 'This is a draft post for testing purposes.', 6, 2, 0, 0)
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- 댓글 테스트 데이터
INSERT INTO COMMENTS (ID, POST_ID, AUTHOR_ID, CONTENT, STATUS)
VALUES (1, 1, 6, 'Great post! Very informative.', 'ACTIVE'),
       (2, 1, 7, 'Thank you for sharing this.', 'ACTIVE'),
       (3, 2, 5, 'Nice work on this board system.', 'ACTIVE')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- 첨부파일 테스트 데이터
INSERT INTO ATTACHMENTS (ID, POST_ID, ORIGINAL_NAME, STORED_NAME, FILE_PATH, FILE_SIZE, CONTENT_TYPE)
VALUES (1, 1, 'test-document.pdf', 'test-doc-20241201.pdf', '/uploads/files/test-doc-20241201.pdf', 1024000, 'application/pdf'),
       (2, 1, 'sample-image.jpg', 'sample-img-20241201.jpg', '/uploads/images/sample-img-20241201.jpg', 256000, 'image/jpeg')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- OAuth2 사용자 연결 테스트 데이터 추가
INSERT INTO USER_CONNECTION (EMAIL, PROVIDER, PROVIDER_ID, DISPLAY_NAME, PROFILE_URL, IMAGE_URL, ACCESS_TOKEN, EXPIRE_TIME)
VALUES ('genius@primavera.com', 1, 'google_123456', 'Genius Choi', 'https://plus.google.com/123456', 'https://lh3.googleusercontent.com/a/default-user', 'google_access_token_123', 1735689600000),
       ('admin@primavera.com', 2, 'facebook_789012', 'Administrator', 'https://www.facebook.com/789012', 'https://graph.facebook.com/789012/picture', 'facebook_access_token_456', 1735689600000),
       ('manager@primavera.com', 3, 'github_345678', 'Manager User', 'https://github.com/345678', 'https://avatars.githubusercontent.com/u/345678', 'github_access_token_789', 1735689600000)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);