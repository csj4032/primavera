DROP TABLE IF EXISTS ROLE;

CREATE TABLE IF NOT EXISTS ROLE
(
    ID   INT(11)    NOT NULL AUTO_INCREMENT,
    TYPE TINYINT(3) NOT NULL,
    PRIMARY KEY (ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;