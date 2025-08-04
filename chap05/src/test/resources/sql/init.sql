CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     INT      DEFAULT 1,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_USERS_EMAIL (EMAIL),
    INDEX IDX_USERS_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ROLES
(
    ID   BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE INT NOT NULL,
    INDEX IDX_ROLES_TYPE (TYPE)
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
    UNIQUE KEY UK_USER_ROLE (USER_ID, ROLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS WINNERS
(
    ID     BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME   VARCHAR(50) NOT NULL,
    YEAR   INT         NOT NULL,
    SPORT  VARCHAR(50) NOT NULL,
    PRIZE  VARCHAR(50) NOT NULL,
    AMOUNT DECIMAL(10, 2) DEFAULT 0.00,
    INDEX IDX_WINNERS_YEAR (YEAR),
    INDEX IDX_WINNERS_SPORT (SPORT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 표준 권한 테이블 데이터
INSERT INTO ROLES (ID, TYPE)
VALUES (1, 1), -- ADMINISTRATOR
       (2, 2), -- MANAGER  
       (3, 3)  -- USER
ON DUPLICATE KEY UPDATE TYPE = VALUES(TYPE);

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