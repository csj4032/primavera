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

INSERT INTO ROLES (ID, TYPE)
VALUES (1, 1),
       (2, 2),
       (3, 3)
ON DUPLICATE KEY UPDATE TYPE = VALUES(TYPE);

INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'genius@primavera.com', '{noop}test', 'Genius', 1),
       (2, 'admin@primavera.com', '{noop}test', 'Admin', 1),
       (3, 'user@primavera.com', '{noop}test', 'User', 1),
       (4, 'son@primavera.com', '{noop}test', 'Son', 1),
       (5, 'messi@primavera.com', '{noop}test', 'Messi', 1),
       (6, 'ronaldo@primavera.com', '{noop}test', 'Ronaldo', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 1),
       (2, 2),
       (3, 3),
       (4, 3),
       (5, 3),
       (6, 3)
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

INSERT INTO WINNERS (NAME, YEAR, SPORT, PRIZE, AMOUNT)
VALUES ('Lionel Messi', 2021, 'Football', 'Ballon', 10.00),
       ('Cristiano Ronaldo', 2021, 'Football', 'Ballon', 100.00),
       ('Novak Djokovic', 2021, 'Tennis', 'Grand Slam', 110.00),
       ('Tom Brady', 2021, 'American Football', 'Super Bowl MVP', 10.00),
       ('Simone Biles', 2021, 'Gymnastics', 'Olympic Gold', 10.00),
       ('LeBron James', 2021, 'Basketball', 'NBA Championship', 10.00),
       ('Lewis Hamilton', 2021, 'Formula 1', 'World Champion', 10.00),
       ('Tiger Woods', 2021, 'Golf', 'Masters Tournament', 10.00),
       ('Serena Williams', 2021, 'Tennis', 'Wimbledon Champion', 1000.00),
       ('Roger Federer', 2021, 'Tennis', 'Australian Open Champion', 1000.00),
       ('Kylian Mbappé', 2021, 'Football', 'FIFA World Cup', 1000.00),
       ('Virat Kohli', 2021, 'Cricket', 'ICC World Cup', 100.00),
       ('Usain Bolt', 2021, 'Athletics', 'Olympic Gold', 100.00)