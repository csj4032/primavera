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


INSERT INTO USERS(EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT)
VALUES ('genius@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Genius', 'A', NOW()),
       ('son@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Son', 'A', NOW()),
       ('messi@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Messi', 'A', NOW()),
       ('ronaldo@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Ronaldo', 'A', NOW());
