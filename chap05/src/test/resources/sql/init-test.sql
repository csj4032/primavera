-- ==============================================
-- Chapter 05 - Data Access with MyBatis Test Data
-- Uses primavera_test database (TestContainers)
-- Focus: MyBatis integration, SQL mapping, database operations
-- ==============================================

-- 공통 사용자 테이블
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

-- Chapter 05 특화 테이블 - MyBatis 매핑 테스트용
CREATE TABLE IF NOT EXISTS MYBATIS_SAMPLES
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME       VARCHAR(100) NOT NULL,
    VALUE      VARCHAR(255),
    TYPE       VARCHAR(50) DEFAULT 'DEFAULT',
    ACTIVE     BOOLEAN     DEFAULT TRUE,
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    INDEX IDX_TYPE (TYPE),
    INDEX IDX_ACTIVE (ACTIVE)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터 - Chapter 05: MyBatis Integration
INSERT INTO ROLES (ID, NAME, DESCRIPTION, TYPE)
VALUES (1, 'ROLE_ADMIN', 'MyBatis 테스트 관리자', 1),
       (2, 'ROLE_USER', 'MyBatis 테스트 사용자', 3)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'mybatis@primavera.com', '{noop}test', 'MyBatisTest', 'ACTIVE'),
       (2, 'mapper@primavera.com', '{noop}test', 'MapperTest', 'ACTIVE'),
       (3, 'sql@primavera.com', '{noop}test', 'SQLTest', 'ACTIVE')
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), -- mybatis -> admin, user
       (2, 2),         -- mapper -> user
       (3, 2)          -- sql -> user
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

INSERT INTO MYBATIS_SAMPLES (ID, NAME, VALUE, TYPE, ACTIVE)
VALUES (1, 'Sample Config', 'test-value-1', 'CONFIG', TRUE),
       (2, 'Sample Setting', 'test-value-2', 'SETTING', TRUE),
       (3, 'Inactive Sample', 'test-value-3', 'INACTIVE', FALSE)
ON DUPLICATE KEY UPDATE ID = VALUES(ID);