-- Chapter 04 MyBatis 테스트를 위한 데이터베이스 스키마

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT(20)   NOT NULL AUTO_INCREMENT,
    EMAIL      VARCHAR(50)  NOT NULL,
    PASSWORD   VARCHAR(100) NOT NULL,
    NICKNAME   VARCHAR(45)  NOT NULL,
    STATUS     CHAR(1)      NOT NULL DEFAULT 'A',
    CREATED_AT DATETIME     NOT NULL,
    UPDATED_AT DATETIME              DEFAULT NULL,
    PRIMARY KEY (ID),
    UNIQUE KEY EMAIL_UNIQUE (EMAIL)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 역할 테이블
CREATE TABLE IF NOT EXISTS ROLE
(
    ID   BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE VARCHAR(255) NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자-역할 매핑 테이블
CREATE TABLE IF NOT EXISTS USER_ROLES
(
    ID         BIGINT(20) NOT NULL AUTO_INCREMENT,
    USER_ID    BIGINT(20) NOT NULL,
    ROLE_ID    BIGINT(20) NOT NULL,
    CREATED_AT DATETIME   NOT NULL,
    PRIMARY KEY (ID),
    FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    FOREIGN KEY (ROLE_ID) REFERENCES ROLE (ID) ON DELETE CASCADE,
    UNIQUE KEY USER_ROLE_UNIQUE (USER_ID, ROLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Winner 테이블 (MyBatis 테스트용)
CREATE TABLE IF NOT EXISTS WINNERS
(
    ID     BIGINT(20)  NOT NULL AUTO_INCREMENT,
    NAME   VARCHAR(50) NOT NULL,
    YEAR   INT         NOT NULL,
    SPORT  VARCHAR(50) NOT NULL,
    PRIZE  VARCHAR(50) NOT NULL,
    AMOUNT DECIMAL(10, 2) DEFAULT 0.00,
    PRIMARY KEY (ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 초기 데이터 삽입
INSERT INTO USERS(EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT)
VALUES ('genius@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Genius', 'A', NOW()),
       ('admin@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Admin', 'A', NOW()),
       ('user@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'User', 'A', NOW());

INSERT INTO ROLE(TYPE)
VALUES ('ROLE_ADMIN'),
       ('ROLE_USER'),
       ('ROLE_MANAGER');

INSERT INTO USER_ROLES(USER_ID, ROLE_ID, CREATED_AT)
VALUES (1, 1, NOW()), -- genius -> ADMIN
       (1, 2, NOW()), -- genius -> USER
       (2, 1, NOW()), -- admin -> ADMIN
       (3, 2, NOW()); -- user -> USER

INSERT INTO WINNERS(NAME, YEAR, SPORT, PRIZE, AMOUNT)
VALUES ('Lionel Messi', 2023, 'Football', 'Ballon d''Or', 1000000.00),
       ('Erling Haaland', 2023, 'Football', 'Golden Boot', 500000.00),
       ('Lewis Hamilton', 2023, 'Formula 1', 'World Championship', 2000000.00),
       ('Serena Williams', 2023, 'Tennis', 'Wimbledon', 750000.00),
       ('Tiger Woods', 2023, 'Golf', 'Masters Tournament', 1500000.00);