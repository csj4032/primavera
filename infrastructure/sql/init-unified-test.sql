-- ==============================================
-- Primavera Project - 통합 데이터베이스 초기화 스크립트 (Test)
-- MariaDB 11.4.7 - TestContainers 최적화 구조
-- ==============================================

-- 기본 데이터베이스 설정
ALTER DATABASE primavera CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==============================================
-- 테스트용 통합 데이터베이스 생성
-- ==============================================

-- 테스트 전용 데이터베이스 (경량화)
CREATE DATABASE IF NOT EXISTS primavera_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==============================================
-- 권한 설정
-- ==============================================

GRANT ALL PRIVILEGES ON primavera.* TO 'primavera'@'%' IDENTIFIED BY 'primavera';
GRANT ALL PRIVILEGES ON primavera_test.* TO 'primavera'@'%' IDENTIFIED BY 'primavera';

FLUSH PRIVILEGES;

-- ==============================================
-- 테스트용 데이터베이스 (primavera_test)
-- 모든 챕터의 TestContainers 테스트에 사용
-- ==============================================

USE primavera_test;

-- 공통 테스트 사용자 테이블
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

-- 테스트용 게시글 테이블
CREATE TABLE IF NOT EXISTS POSTS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    AUTHOR_ID  BIGINT       NOT NULL,
    TITLE      VARCHAR(255) NOT NULL,
    CONTENT    LONGTEXT     NOT NULL,
    STATUS     VARCHAR(20) DEFAULT 'PUBLISHED',
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (AUTHOR_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    INDEX IDX_AUTHOR_ID (AUTHOR_ID),
    INDEX IDX_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트용 댓글 테이블
CREATE TABLE IF NOT EXISTS COMMENTS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    POST_ID    BIGINT   NOT NULL,
    AUTHOR_ID  BIGINT   NOT NULL,
    CONTENT    LONGTEXT NOT NULL,
    STATUS     VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (POST_ID) REFERENCES POSTS (ID) ON DELETE CASCADE,
    FOREIGN KEY (AUTHOR_ID) REFERENCES USERS (ID) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트용 파일 업로드 테이블 (간소화)
CREATE TABLE IF NOT EXISTS FILE_UPLOADS
(
    ID                BIGINT AUTO_INCREMENT PRIMARY KEY,
    POST_ID           BIGINT,
    ORIGINAL_FILENAME VARCHAR(255) NOT NULL,
    STORED_FILENAME   VARCHAR(255) NOT NULL,
    FILE_PATH         VARCHAR(500) NOT NULL,
    FILE_SIZE         BIGINT       NOT NULL,
    CONTENT_TYPE      VARCHAR(100),
    UPLOAD_STATUS     VARCHAR(20) DEFAULT 'UPLOADED',
    CREATED_AT        DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (POST_ID) REFERENCES POSTS (ID) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트용 기본 아이템 테이블
CREATE TABLE IF NOT EXISTS ITEMS
(
    ID     BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE   VARCHAR(10)  NOT NULL,
    NAME   VARCHAR(100) NOT NULL,
    PRICE  DECIMAL(10, 2) DEFAULT 0.00,
    STATUS VARCHAR(20)    DEFAULT 'ACTIVE'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트용 주문 테이블
CREATE TABLE IF NOT EXISTS ORDERS
(
    ID           BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID      BIGINT         NOT NULL,
    TOTAL_AMOUNT DECIMAL(12, 2) NOT NULL,
    STATUS       VARCHAR(20) DEFAULT 'PENDING',
    CREATED_AT   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ==============================================
-- 테스트 데이터 삽입
-- ==============================================

INSERT INTO ROLES (ID, NAME, DESCRIPTION, TYPE)
VALUES (1, 'ROLE_ADMIN', '테스트 관리자', 1),
       (2, 'ROLE_USER', '테스트 사용자', 3)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'test-admin@primavera.com', '{noop}test', 'TestAdmin', 'ACTIVE'),
       (2, 'test-user@primavera.com', '{noop}test', 'TestUser', 'ACTIVE'),
       (3, 'test-user2@primavera.com', '{noop}test', 'TestUser2', 'ACTIVE')
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2),
       (2, 2),
       (3, 2)
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

INSERT INTO POSTS (ID, AUTHOR_ID, TITLE, CONTENT, STATUS)
VALUES (1, 1, 'Test Post 1', 'This is a test content for automated testing.', 'PUBLISHED'),
       (2, 1, 'Test Post 2', 'Another test content with different data.', 'PUBLISHED'),
       (3, 2, 'User Test Post', 'User test content for validation.', 'PUBLISHED'),
       (4, 3, 'Draft Post', 'This is a draft post for testing.', 'DRAFT')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

INSERT INTO COMMENTS (POST_ID, AUTHOR_ID, CONTENT, STATUS)
VALUES (1, 2, 'This is a test comment.', 'ACTIVE'),
       (1, 3, 'Another test comment.', 'ACTIVE'),
       (2, 2, 'Comment on second post.', 'ACTIVE')
ON DUPLICATE KEY UPDATE POST_ID = VALUES(POST_ID);

INSERT INTO ITEMS (ID, TYPE, NAME, PRICE, STATUS)
VALUES (1, 'BOOK', 'Test Book', 25000.00, 'ACTIVE'),
       (2, 'ALBUM', 'Test Album', 15000.00, 'ACTIVE'),
       (3, 'MOVIE', 'Test Movie', 12000.00, 'ACTIVE')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

INSERT INTO ORDERS (ID, USER_ID, TOTAL_AMOUNT, STATUS)
VALUES (1, 2, 25000.00, 'COMPLETED'),
       (2, 3, 15000.00, 'PENDING'),
       (3, 2, 37000.00, 'PROCESSING')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- 샘플 파일 업로드 데이터
INSERT INTO FILE_UPLOADS (ID, POST_ID, ORIGINAL_FILENAME, STORED_FILENAME, FILE_PATH, FILE_SIZE, CONTENT_TYPE, UPLOAD_STATUS)
VALUES (1, 1, 'test-document.pdf', 'test_20240101_abc123.pdf', '/test/uploads/1/', 1024000, 'application/pdf', 'UPLOADED'),
       (2, 2, 'test-image.jpg', 'test_20240102_def456.jpg', '/test/uploads/2/', 512000, 'image/jpeg', 'UPLOADED')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- ==============================================
-- 인덱스 최적화
-- ==============================================

ANALYZE TABLE USERS, ROLES, USER_ROLES, POSTS, COMMENTS, FILE_UPLOADS, ITEMS, ORDERS;

-- 기본 데이터베이스로 복귀
USE primavera;