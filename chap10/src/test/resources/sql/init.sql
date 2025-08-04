-- ==============================================
-- Chapter 10 - OAuth2 Social Login Test Data
-- Uses primavera_test database (TestContainers)
-- Focus: OAuth2 integration, social login, provider management
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

-- Chapter 10 특화 테이블 - OAuth2 소셜 로그인 연결 정보
CREATE TABLE IF NOT EXISTS USER_CONNECTIONS
(
    ID               BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID          BIGINT       NOT NULL,
    PROVIDER_ID      VARCHAR(50)  NOT NULL,
    PROVIDER_USER_ID VARCHAR(100) NOT NULL,
    DISPLAY_NAME     VARCHAR(100),
    PROFILE_URL      VARCHAR(500),
    IMAGE_URL        VARCHAR(500),
    ACCESS_TOKEN     TEXT,
    REFRESH_TOKEN    TEXT,
    EXPIRE_TIME      BIGINT,
    CREATED_AT       DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    UNIQUE KEY UNIQUE_PROVIDER_USER (PROVIDER_ID, PROVIDER_USER_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- OAuth2 클라이언트 등록 정보 테이블
CREATE TABLE IF NOT EXISTS OAUTH2_CLIENTS
(
    ID                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    CLIENT_ID             VARCHAR(100) UNIQUE NOT NULL,
    CLIENT_SECRET         VARCHAR(255),
    PROVIDER_ID           VARCHAR(50)         NOT NULL,
    AUTHORIZATION_URI     VARCHAR(500),
    TOKEN_URI             VARCHAR(500),
    USER_INFO_URI         VARCHAR(500),
    REDIRECT_URI          VARCHAR(500),
    SCOPE                 VARCHAR(255) DEFAULT 'profile,email',
    ACTIVE                BOOLEAN      DEFAULT TRUE,
    CREATED_AT            DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX IDX_PROVIDER_ID (PROVIDER_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터 - Chapter 10: OAuth2 Social Login
INSERT INTO ROLES (ID, NAME, DESCRIPTION, TYPE)
VALUES (1, 'ROLE_ADMIN', 'OAuth2 테스트 관리자', 1),
       (2, 'ROLE_MANAGER', 'OAuth2 테스트 매니저', 2),
       (3, 'ROLE_USER', 'OAuth2 테스트 사용자', 3)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 표준 사용자 테이블 데이터
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'genius@primavera.com', '{noop}test', 'Genius', 1),
       (2, 'admin@primavera.com', '{noop}test', 'Admin', 1),
       (3, 'user@primavera.com', '{noop}test', 'User', 1),
       (4, 'son@primavera.com', '{noop}test', 'Son', 1),
       (5, 'messi@primavera.com', '{noop}test', 'Messi', 1),
       (6, 'ronaldo@primavera.com', '{noop}test', 'Ronaldo', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 표준 사용자-권한 매핑 데이터
INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1),
       (1, 2),
       (1, 3), -- genius -> all roles
       (2, 1),
       (2, 2), -- admin -> admin, manager  
       (3, 3), -- user -> user
       (4, 3),
       (5, 3),
       (6, 3)  -- sports players -> user
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

-- OAuth2 소셜 로그인 연결 테스트 데이터 (스포츠 선수들의 소셜 연결)
INSERT INTO USER_CONNECTIONS (ID, USER_ID, PROVIDER_ID, PROVIDER_USER_ID, DISPLAY_NAME, ACCESS_TOKEN)
VALUES (1, 4, 'kakao', '123456789', 'Son Heung-min', 'test_access_token_kakao'),
       (2, 5, 'google', 'google123456', 'Lionel Messi', 'test_access_token_google'),
       (3, 6, 'github', '987654321', 'Cristiano Ronaldo', 'test_access_token_github')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- OAuth2 클라이언트 설정 테스트 데이터
INSERT INTO OAUTH2_CLIENTS (CLIENT_ID, PROVIDER_ID, AUTHORIZATION_URI, TOKEN_URI, USER_INFO_URI, ACTIVE)
VALUES ('test-kakao-client', 'kakao', 
        'https://kauth.kakao.com/oauth/authorize',
        'https://kauth.kakao.com/oauth/token', 
        'https://kapi.kakao.com/v2/user/me', TRUE),
       ('test-google-client', 'google',
        'https://accounts.google.com/o/oauth2/auth',
        'https://oauth2.googleapis.com/token',
        'https://www.googleapis.com/oauth2/v2/userinfo', TRUE)
ON DUPLICATE KEY UPDATE CLIENT_ID = VALUES(CLIENT_ID);