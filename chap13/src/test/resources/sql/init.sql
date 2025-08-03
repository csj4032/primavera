CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     INT      DEFAULT 1,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_USER_EMAIL (EMAIL)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ROLES
(
    ID   BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE INT NOT NULL
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
    UNIQUE KEY UNIQUE_USER_ROLE (USER_ID, ROLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS USER_CONNECTION
(
    ID           BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL        VARCHAR(100) NOT NULL,
    PROVIDER     INT          NOT NULL,
    PROVIDER_ID  VARCHAR(255) NOT NULL,
    DISPLAY_NAME VARCHAR(100),
    PROFILE_URL  VARCHAR(500),
    IMAGE_URL    VARCHAR(500),
    ACCESS_TOKEN VARCHAR(1000),
    EXPIRE_TIME  BIGINT,
    FOREIGN KEY (EMAIL) REFERENCES USERS (EMAIL) ON DELETE CASCADE,
    UNIQUE KEY UNIQUE_PROVIDER_USER (EMAIL, PROVIDER),
    INDEX IDX_USER_CONNECTION_EMAIL (EMAIL),
    INDEX IDX_USER_CONNECTION_PROVIDER (PROVIDER)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ARTICLE
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    P_ID       BIGINT,
    REFERENCE  BIGINT       NOT NULL,
    STEP       INT          NOT NULL DEFAULT 1,
    LEVEL      INT          NOT NULL DEFAULT 1,
    SUBJECT    VARCHAR(200) NOT NULL,
    AUTHOR     BIGINT       NOT NULL,
    STATUS     INT          NOT NULL DEFAULT 1,
    HIT        INT                   DEFAULT 0,
    RECOMMEND  INT                   DEFAULT 0,
    DISAPPROVE INT                   DEFAULT 0,
    CREATED_AT DATETIME              DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (P_ID) REFERENCES ARTICLE (ID) ON DELETE CASCADE,
    FOREIGN KEY (AUTHOR) REFERENCES USERS (ID),
    INDEX IDX_ARTICLE_P_ID (P_ID),
    INDEX IDX_ARTICLE_REFERENCE (REFERENCE),
    INDEX IDX_ARTICLE_AUTHOR (AUTHOR),
    INDEX IDX_ARTICLE_STATUS (STATUS),
    INDEX IDX_ARTICLE_STEP (STEP)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ARTICLE_CONTENT
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID BIGINT   NOT NULL,
    CONTENTS   LONGTEXT NOT NULL,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID) ON DELETE CASCADE,
    INDEX IDX_ARTICLE_CONTENT_ARTICLE_ID (ARTICLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ARTICLE_COMMENT
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID BIGINT NOT NULL,
    LEVEL      INT    NOT NULL DEFAULT 1,
    STEP       INT    NOT NULL DEFAULT 1,
    COMMENT    TEXT   NOT NULL,
    AUTHOR     BIGINT NOT NULL,
    STATUS     INT    NOT NULL DEFAULT 1,
    CREATED_AT DATETIME        DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID) ON DELETE CASCADE,
    FOREIGN KEY (AUTHOR) REFERENCES USERS (ID),
    INDEX IDX_ARTICLE_COMMENT_ARTICLE_ID (ARTICLE_ID),
    INDEX IDX_ARTICLE_COMMENT_AUTHOR (AUTHOR)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ARTICLE_ATTACHMENT
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID BIGINT        NOT NULL,
    NAME       VARCHAR(255)  NOT NULL,
    PATH       VARCHAR(1000) NOT NULL,
    SIZE       BIGINT        NOT NULL DEFAULT 0,
    CREATED_AT DATETIME               DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID) ON DELETE CASCADE,
    INDEX IDX_ARTICLE_ATTACHMENT_ARTICLE_ID (ARTICLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO ROLES (ID, TYPE)
VALUES (1, 1),
       (2, 2),
       (3, 3)
ON DUPLICATE KEY UPDATE TYPE = VALUES(TYPE);

INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT, UPDATED_AT)
VALUES (1, 'admin@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Admin', 1, NOW(), NOW()),
       (2, 'manager@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Manager', 1, NOW(), NOW()),
       (3, 'user@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'User', 1, NOW(), NOW()),
       (4, 'genius@primavera.com', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Genius', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 2),
       (2, 3),
       (3, 3),
       (4, 1),
       (4, 2),
       (4, 3)
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

INSERT INTO USER_CONNECTION (ID, EMAIL, PROVIDER, PROVIDER_ID, DISPLAY_NAME, PROFILE_URL, IMAGE_URL)
VALUES (1, 'admin@primavera.com', 1, 'google_admin_123', 'Admin', 'https://profile.google.com/admin', 'https://profile.google.com/admin/photo.jpg'),
       (2, 'manager@primavera.com', 2, 'facebook_manager_456', 'Manager', 'https://profile.facebook.com/manager', 'https://profile.facebook.com/manager/photo.jpg'),
       (3, 'user@primavera.com', 3, 'kakao_user_789', 'User', 'https://profile.kakao.com/user', 'https://profile.kakao.com/user/photo.jpg'),
       (4, 'genius@primavera.com', 1, 'google_genius_999', 'Genius', 'https://profile.google.com/genius', 'https://profile.google.com/genius/photo.jpg')
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO ARTICLE (ID, P_ID, REFERENCE, STEP, LEVEL, SUBJECT, AUTHOR, STATUS, HIT, CREATED_AT)
VALUES (1, NULL, 1, 1, 1, '첫 번째 원글', 1, 1, 10, NOW()),
       (2, NULL, 2, 1, 1, '두 번째 원글', 2, 1, 5, NOW()),
       (3, NULL, 3, 1, 1, '세 번째 원글', 1, 1, 15, NOW()),
       (4, NULL, 4, 1, 1, '네 번째 원글', 2, 1, 8, NOW()),
       (5, NULL, 5, 1, 1, '다섯 번째 원글', 1, 1, 12, NOW()),
       (6, 1, 1, 2, 2, 'RE: 첫 번째 원글', 2, 1, 3, NOW()),
       (7, 1, 1, 3, 2, 'RE: 첫 번째 원글 (2)', 1, 1, 2, NOW()),
       (8, 6, 1, 4, 3, 'RE: RE: 첫 번째 원글', 1, 1, 1, NOW()),
       (9, 2, 2, 2, 2, 'RE: 두 번째 원글', 1, 1, 4, NOW()),
       (10, 2, 2, 3, 2, 'RE: 두 번째 원글 (2)', 2, 1, 2, NOW()),
       (11, 9, 2, 4, 3, 'RE: RE: 두 번째 원글', 2, 1, 1, NOW()),
       (12, 9, 2, 5, 3, 'RE: RE: 두 번째 원글 (2)', 1, 1, 0, NOW()),
       (13, 3, 3, 2, 2, 'RE: 세 번째 원글', 2, 1, 6, NOW()),
       (14, 3, 3, 3, 2, 'RE: 세 번째 원글 (2)', 1, 1, 3, NOW()),
       (15, 3, 3, 4, 2, 'RE: 세 번째 원글 (3)', 2, 1, 2, NOW()),
       (16, 13, 3, 5, 3, 'RE: RE: 세 번째 원글', 1, 1, 1, NOW()),
       (17, 4, 4, 2, 2, 'RE: 네 번째 원글', 1, 1, 2, NOW()),
       (18, 4, 4, 3, 2, 'RE: 네 번째 원글 (2)', 2, 1, 1, NOW()),
       (19, 17, 4, 4, 3, 'RE: RE: 네 번째 원글', 2, 1, 0, NOW()),
       (20, 5, 5, 2, 2, 'RE: 다섯 번째 원글', 2, 1, 3, NOW()),
       (21, 5, 5, 3, 2, 'RE: 다섯 번째 원글 (2)', 1, 1, 2, NOW()),
       (22, 5, 5, 4, 2, 'RE: 다섯 번째 원글 (3)', 2, 1, 1, NOW()),
       (23, 20, 5, 5, 3, 'RE: RE: 다섯 번째 원글', 1, 1, 1, NOW()),
       (24, 21, 5, 6, 3, 'RE: RE: 다섯 번째 원글 (2)', 2, 1, 0, NOW())
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

INSERT INTO ARTICLE_CONTENT (ID, ARTICLE_ID, CONTENTS)
VALUES (1, 1, '첫 번째 원글의 내용입니다. 이것은 테스트 게시글입니다.'),
       (2, 2, '두 번째 원글의 내용입니다. Advanced JPA 기능을 테스트합니다.'),
       (3, 3, '세 번째 원글의 내용입니다. 계층형 구조를 확인합니다.'),
       (4, 4, '네 번째 원글의 내용입니다. 첨부파일 기능도 포함됩니다.'),
       (5, 5, '다섯 번째 원글의 내용입니다. 다양한 기능을 테스트합니다.'),
       (6, 6, '첫 번째 원글에 대한 답글입니다.'),
       (7, 7, '첫 번째 원글에 대한 두 번째 답글입니다.'),
       (8, 8, '첫 번째 원글의 답글에 대한 답글입니다.'),
       (9, 9, '두 번째 원글에 대한 답글입니다.'),
       (10, 10, '두 번째 원글에 대한 두 번째 답글입니다.')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

INSERT INTO ARTICLE_ATTACHMENT (ID, ARTICLE_ID, NAME, PATH, SIZE)
VALUES (1, 1, 'document1.pdf', '/uploads/files/document1.pdf', 1024000),
       (2, 1, 'image1.jpg', '/uploads/images/image1.jpg', 256000),
       (3, 3, 'presentation.pptx', '/uploads/files/presentation.pptx', 2048000),
       (4, 4, 'data.xlsx', '/uploads/files/data.xlsx', 512000),
       (5, 5, 'video.mp4', '/uploads/videos/video.mp4', 10240000)
ON DUPLICATE KEY UPDATE ID = VALUES(ID);