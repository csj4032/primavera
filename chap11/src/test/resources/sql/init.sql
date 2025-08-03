-- ==============================================
-- Chapter 11 - Advanced Board System Test Data
-- Uses primavera_test database (TestContainers)
-- Focus: Board system, post management, file attachments
-- ==============================================

-- 공통 사용자 테이블
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
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL       VARCHAR(100) NOT NULL,
    PROVIDER    INT          NOT NULL,
    PROVIDER_ID VARCHAR(100) NOT NULL,
    PROFILE_URL VARCHAR(500),
    IMAGE_URL   VARCHAR(500),
    CREATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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

-- 댓글 테이블 (간단한 구조)
CREATE TABLE IF NOT EXISTS COMMENTS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    POST_ID    BIGINT      NOT NULL,
    AUTHOR_ID  BIGINT      NOT NULL,
    CONTENT    TEXT        NOT NULL,
    STATUS     INT DEFAULT 1,
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

-- 테스트 데이터 - Chapter 11: Advanced Board System
INSERT INTO ROLES (ID, NAME, DESCRIPTION, TYPE)
VALUES (1, 'ROLE_ADMIN', 'Board 테스트 관리자', 1),
       (2, 'ROLE_MANAGER', 'Board 테스트 매니저', 2),
       (3, 'ROLE_USER', 'Board 테스트 사용자', 3)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'board@primavera.com', '{noop}test', 'BoardAdmin', 1),
       (2, 'writer@primavera.com', '{noop}test', 'Writer', 1),
       (3, 'reader@primavera.com', '{noop}test', 'Reader', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), (1, 3), -- board -> all roles
       (2, 2),                 -- writer -> manager
       (3, 3)                  -- reader -> user
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

-- 게시글 테스트 데이터
INSERT INTO POSTS (ID, SUBJECT, CONTENTS, WRITER_ID, STATUS, VIEW_COUNT, LIKE_COUNT)
VALUES (1, 'Board System Test Post 1', 'This is a test post for the board system. Content includes various features.', 1, 1, 100, 15),
       (2, 'Board System Test Post 2', 'Another test post with different content and features.', 2, 1, 75, 8),
       (3, 'Draft Post', 'This is a draft post for testing purposes.', 2, 2, 0, 0)
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- 댓글 테스트 데이터
INSERT INTO COMMENTS (ID, POST_ID, AUTHOR_ID, CONTENT, STATUS)
VALUES (1, 1, 2, 'Great post! Very informative.', 1),
       (2, 1, 3, 'Thank you for sharing this.', 1),
       (3, 2, 1, 'Nice work on this board system.', 1)
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- 첨부파일 테스트 데이터
INSERT INTO ATTACHMENTS (ID, POST_ID, ORIGINAL_NAME, STORED_NAME, FILE_PATH, FILE_SIZE, CONTENT_TYPE)
VALUES (1, 1, 'test-document.pdf', 'test-doc-20241201.pdf', '/uploads/files/test-doc-20241201.pdf', 1024000, 'application/pdf'),
       (2, 1, 'sample-image.jpg', 'sample-img-20241201.jpg', '/uploads/images/sample-img-20241201.jpg', 256000, 'image/jpeg')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- OAuth2 사용자 연결 테스트 데이터
INSERT INTO USER_CONNECTION (EMAIL, PROVIDER, PROVIDER_ID, PROFILE_URL, IMAGE_URL)
VALUES ('board@primavera.com', 1, 'google_123456', 'https://plus.google.com/123456', 'https://lh3.googleusercontent.com/a/default-user'),
       ('writer@primavera.com', 2, 'facebook_789012', 'https://www.facebook.com/789012', 'https://graph.facebook.com/789012/picture'),
       ('reader@primavera.com', 3, 'github_345678', 'https://github.com/345678', 'https://avatars.githubusercontent.com/u/345678')
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);