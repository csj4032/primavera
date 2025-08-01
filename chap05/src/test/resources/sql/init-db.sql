-- ==============================================
-- Chapter 05 - HikariCP Connection Pool Test Data
-- Uses primavera_test database (TestContainers)
-- ==============================================

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

-- Chapter 05 특화 테이블 - BASIC 접두사 사용
CREATE TABLE IF NOT EXISTS BASIC_WINNERS
(
    ID     BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME   VARCHAR(50) NOT NULL,
    YEAR   INT         NOT NULL,
    SPORT  VARCHAR(50) NOT NULL,
    PRIZE  VARCHAR(50) NOT NULL,
    AMOUNT DECIMAL(10, 2) DEFAULT 0.00,
    INDEX IDX_YEAR (YEAR),
    INDEX IDX_SPORT (SPORT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 기본 테스트 데이터
INSERT INTO ROLES (ID, NAME, DESCRIPTION, TYPE)
VALUES (1, 'ROLE_ADMIN', '테스트 관리자', 1),
       (2, 'ROLE_MANAGER', '테스트 매니저', 2),
       (3, 'ROLE_USER', '테스트 사용자', 3)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'genius@primavera.com', '{noop}test', 'Genius', 'ACTIVE'),
       (2, 'admin@primavera.com', '{noop}test', 'Admin', 'ACTIVE'),
       (3, 'user@primavera.com', '{noop}test', 'User', 'ACTIVE'),
       (4, 'son@primavera.com', '{noop}test', 'Son', 'ACTIVE'),
       (5, 'messi@primavera.com', '{noop}test', 'Messi', 'ACTIVE'),
       (6, 'ronaldo@primavera.com', '{noop}test', 'Ronaldo', 'ACTIVE')
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), (1, 3), -- genius -> all roles
       (2, 1), (2, 2),         -- admin -> admin, manager
       (3, 3),                 -- user -> user
       (4, 3), (5, 3), (6, 3)  -- sports players -> user
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

INSERT INTO BASIC_WINNERS (ID, NAME, YEAR, SPORT, PRIZE, AMOUNT)
VALUES (1, 'Lionel Messi', 2023, 'Football', 'Ballon d''Or', 1000000.00),
       (2, 'Erling Haaland', 2023, 'Football', 'Golden Boot', 500000.00),
       (3, 'Lewis Hamilton', 2023, 'Formula 1', 'World Championship', 2000000.00),
       (4, 'Serena Williams', 2023, 'Tennis', 'Wimbledon', 750000.00),
       (5, 'Tiger Woods', 2023, 'Golf', 'Masters Tournament', 1500000.00)
ON DUPLICATE KEY UPDATE ID = VALUES(ID);