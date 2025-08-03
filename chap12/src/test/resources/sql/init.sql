-- ==============================================
-- Chapter 12 - Advanced Board System with Hierarchical Comments Test Data
-- Uses primavera_test database (TestContainers)
-- Focus: Hierarchical comments, advanced security, board management
-- ==============================================

-- 사용자 테이블 (User 클래스 기준으로 정리)
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255),
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     INT                 DEFAULT 1,    -- UserStatus enum: 1=ON, 2=BLOCK, 3=DORMANT, 4=LEAVE
    CREATED_AT DATETIME            DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_USERS_EMAIL (EMAIL),
    INDEX IDX_USERS_NICKNAME (NICKNAME),
    INDEX IDX_USERS_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 역할 테이블 (Role 클래스 기준으로 정리)
CREATE TABLE IF NOT EXISTS ROLES
(
    ID   BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE INT NOT NULL    -- RoleType enum: 1=ADMINISTRATOR, 2=MANAGER, 3=USER
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자 소셜 연결 테이블 (UserConnection 클래스 기준)
CREATE TABLE IF NOT EXISTS USER_CONNECTION
(
    ID           BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL        VARCHAR(100) NOT NULL,
    PROVIDER     INT          NOT NULL,  -- ProviderType enum
    PROVIDER_ID  VARCHAR(255) NOT NULL,
    DISPLAY_NAME VARCHAR(100),
    PROFILE_URL  VARCHAR(500),
    IMAGE_URL    VARCHAR(500),
    ACCESS_TOKEN VARCHAR(1000),
    EXPIRE_TIME  BIGINT,
    
    FOREIGN KEY (EMAIL) REFERENCES USERS (EMAIL) ON DELETE CASCADE,
    UNIQUE KEY UK_PROVIDER_USER (EMAIL, PROVIDER)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자 역할 매핑 테이블
CREATE TABLE IF NOT EXISTS USER_ROLES
(
    ID      BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID BIGINT NOT NULL,
    ROLE_ID BIGINT NOT NULL,
    
    FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    FOREIGN KEY (ROLE_ID) REFERENCES ROLES (ID) ON DELETE CASCADE,
    UNIQUE KEY UK_USER_ROLE (USER_ID, ROLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시판 카테고리 테이블
CREATE TABLE IF NOT EXISTS BOARD_CATEGORIES
(
    ID               BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME             VARCHAR(100) NOT NULL,
    DESCRIPTION      TEXT,
    SORT_ORDER       INT     DEFAULT 0,
    READ_PERMISSION  VARCHAR(50) DEFAULT 'ROLE_USER',
    WRITE_PERMISSION VARCHAR(50) DEFAULT 'ROLE_USER',
    ADMIN_PERMISSION VARCHAR(50) DEFAULT 'ROLE_MANAGER',
    STATUS           INT DEFAULT 1,
    CREATED_AT       DATETIME    DEFAULT CURRENT_TIMESTAMP,

    INDEX IDX_BOARD_CATEGORIES_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시글 테이블 (계층형 구조)
CREATE TABLE IF NOT EXISTS ARTICLES
(
    ID               BIGINT AUTO_INCREMENT PRIMARY KEY,
    P_ID             BIGINT,                -- 부모 게시글 ID (계층형)
    REFERENCE        BIGINT,                -- 참조 번호 (그룹)
    STEP             INT DEFAULT 0,         -- 답글 순서
    LEVEL            INT DEFAULT 0,         -- 답글 깊이
    SUBJECT          VARCHAR(200) NOT NULL, -- 제목
    AUTHOR           BIGINT       NOT NULL, -- 작성자 ID
    STATUS           INT DEFAULT 1,         -- 상태 (1: 공개, 0: 비공개)
    HIT              INT DEFAULT 0,         -- 조회수
    RECOMMEND        INT DEFAULT 0,         -- 추천수
    DISAPPROVE       INT DEFAULT 0,         -- 비추천수
    CREATED_AT       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (P_ID) REFERENCES ARTICLES (ID),
    FOREIGN KEY (AUTHOR) REFERENCES USERS (ID),
    INDEX IDX_ARTICLES_P_ID (P_ID),
    INDEX IDX_ARTICLES_AUTHOR (AUTHOR),
    INDEX IDX_ARTICLES_REFERENCE (REFERENCE),
    INDEX IDX_ARTICLES_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시글 내용 테이블
CREATE TABLE IF NOT EXISTS ARTICLE_CONTENT
(
    ID             BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID     BIGINT      NOT NULL,
    CONTENTS       LONGTEXT    NOT NULL,
    CREATED_AT     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLES (ID) ON DELETE CASCADE,
    INDEX IDX_ARTICLE_CONTENT_ARTICLE_ID (ARTICLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시글 댓글 테이블 (계층형)
CREATE TABLE IF NOT EXISTS ARTICLE_COMMENT
(
    ID             BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID     BIGINT      NOT NULL,
    LEVEL          INT         DEFAULT 0,
    STEP           INT         DEFAULT 0,
    COMMENT        TEXT        NOT NULL,
    AUTHOR         BIGINT      NOT NULL,
    STATUS         INT         DEFAULT 1,
    CREATED_AT     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLES (ID) ON DELETE CASCADE,
    FOREIGN KEY (AUTHOR) REFERENCES USERS (ID),
    INDEX IDX_ARTICLE_COMMENT_ARTICLE_ID (ARTICLE_ID),
    INDEX IDX_ARTICLE_COMMENT_AUTHOR (AUTHOR)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터 - Chapter 12: Advanced Board with Hierarchical Comments (User, Role 클래스 기준)

-- 역할 데이터 (RoleType enum 기준)
INSERT INTO ROLES (ID, TYPE) VALUES 
(1, 1),  -- ADMINISTRATOR
(2, 2),  -- MANAGER
(3, 3)   -- USER
ON DUPLICATE KEY UPDATE TYPE = VALUES(TYPE);

-- 사용자 데이터 (User 클래스 기준)
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT, UPDATED_AT) VALUES 
(1, 'admin@primavera.com', '{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'SuperAdmin', 1, NOW(), NOW()),
(2, 'manager@primavera.com', '{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'BoardManager', 1, NOW(), NOW()),
(3, 'user1@primavera.com', '{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'TestUser1', 1, NOW(), NOW()),
(4, 'genius@primavera.com', '{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Genius', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 사용자-역할 매핑
INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES 
(1, 1), -- SuperAdmin -> ADMINISTRATOR
(2, 2), -- BoardManager -> MANAGER
(3, 3), -- TestUser1 -> USER
(4, 1)  -- Genius -> ADMINISTRATOR
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

-- 사용자 소셜 연결 정보 (ProviderType enum: 1=FACEBOOK, 2=GITHUB, 3=GOOGLE)
INSERT INTO USER_CONNECTION (ID, EMAIL, PROVIDER, PROVIDER_ID, DISPLAY_NAME, PROFILE_URL, IMAGE_URL) VALUES 
(1, 'admin@primavera.com', 3, 'google_admin_123', 'SuperAdmin', 'https://profile.google.com/admin', 'https://profile.google.com/admin/photo.jpg'),
(2, 'manager@primavera.com', 2, 'github_manager_456', 'BoardManager', 'https://github.com/manager', 'https://github.com/manager.avatar'),
(3, 'user1@primavera.com', 1, 'facebook_user_789', 'TestUser1', 'https://facebook.com/user1', 'https://facebook.com/user1/photo.jpg'),
(4, 'genius@primavera.com', 3, 'google_genius_999', 'Genius', 'https://profile.google.com/genius', 'https://profile.google.com/genius/photo.jpg')
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);
