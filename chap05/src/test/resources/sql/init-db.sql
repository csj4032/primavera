CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     INT(1)              NOT NULL DEFAULT 1,
    CREATED_AT DATETIME                     DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_EMAIL (EMAIL),
    INDEX IDX_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 권한 테이블
CREATE TABLE IF NOT EXISTS ROLES
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE       INT NOT NULL,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자-권한 연결 테이블
CREATE TABLE IF NOT EXISTS USER_ROLES
(
    USER_ID BIGINT NOT NULL,
    ROLE_ID BIGINT NOT NULL,
    PRIMARY KEY (USER_ID, ROLE_ID),
    FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    FOREIGN KEY (ROLE_ID) REFERENCES ROLES (ID) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

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

INSERT INTO ROLES (ID, TYPE)
VALUES (1, 1),
       (2, 2),
       (3, 3);

INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'genius@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'Genius', 1),
       (2, 'admin@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'Admin', 1),
       (3, 'user@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'User', 1),
       (4, 'son@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'Son', 1),
       (5, 'messi@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'Messi', 1),
       (6, 'ronaldo@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'Ronaldo', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

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

INSERT INTO WINNERS (NAME, YEAR, SPORT, PRIZE, AMOUNT)
VALUES ('Lionel Messi', 2023, 'Football', 'Ballon d''Or', 1000000.00),
       ('Erling Haaland', 2023, 'Football', 'Golden Boot', 500000.00),
       ('Lewis Hamilton', 2023, 'Formula 1', 'World Championship', 2000000.00),
       ('Serena Williams', 2023, 'Tennis', 'Wimbledon', 750000.00),
       ('Tiger Woods', 2023, 'Golf', 'Masters Tournament', 1500000.00)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);