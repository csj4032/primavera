-- ==============================================
-- Chapter 12 - Hierarchical Comment Application
-- Database: primavera
-- ==============================================

-- 사용자 테이블
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

-- 계층형 게시글 테이블
CREATE TABLE IF NOT EXISTS ARTICLES
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
    FOREIGN KEY (P_ID) REFERENCES ARTICLES (ID) ON DELETE CASCADE,
    FOREIGN KEY (AUTHOR) REFERENCES USERS (ID),
    INDEX IDX_ARTICLES_P_ID (P_ID),
    INDEX IDX_ARTICLES_REFERENCE (REFERENCE),
    INDEX IDX_ARTICLES_AUTHOR (AUTHOR),
    INDEX IDX_ARTICLES_STATUS (STATUS),
    INDEX IDX_ARTICLES_CREATED_AT (CREATED_AT),
    INDEX IDX_ARTICLES_STEP (STEP)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시글 내용 테이블
CREATE TABLE IF NOT EXISTS ARTICLE_CONTENT
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID BIGINT    NOT NULL,
    CONTENTS   LONGTEXT  NOT NULL,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLES (ID) ON DELETE CASCADE,
    INDEX IDX_ARTICLE_CONTENT_ARTICLE_ID (ARTICLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 계층형 댓글 테이블
CREATE TABLE IF NOT EXISTS ARTICLE_COMMENT
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID BIGINT   NOT NULL,
    LEVEL      INT      NOT NULL DEFAULT 1,
    STEP       INT      NOT NULL DEFAULT 1,
    COMMENT    TEXT     NOT NULL,
    AUTHOR     BIGINT   NOT NULL,
    STATUS     INT      NOT NULL DEFAULT 1,
    CREATED_AT DATETIME          DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLES (ID) ON DELETE CASCADE,
    FOREIGN KEY (AUTHOR) REFERENCES USERS (ID),
    INDEX IDX_ARTICLE_COMMENT_ARTICLE_ID (ARTICLE_ID),
    INDEX IDX_ARTICLE_COMMENT_AUTHOR (AUTHOR),
    INDEX IDX_ARTICLE_COMMENT_STEP (STEP)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 기본 권한 데이터
INSERT INTO ROLES (ID, NAME, DESCRIPTION, TYPE)
VALUES (1, 'ROLE_ADMINISTRATOR', '최고 관리자', 1),
       (2, 'ROLE_MANAGER', '관리자', 2),
       (3, 'ROLE_USER', '일반 사용자', 3)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 기본 사용자 데이터
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'admin@primavera.com', '{noop}admin123', 'Administrator', 1),
       (2, 'manager@primavera.com', '{noop}manager123', 'Manager', 1),
       (3, 'user@primavera.com', '{noop}user123', 'User', 1),
       (4, 'genius@primavera.com', '{noop}test', 'Genius', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 사용자-권한 매핑
INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), (1, 3), -- admin has all roles
       (2, 2), (2, 3),          -- manager has manager and user roles
       (3, 3),                  -- user has user role
       (4, 1), (4, 2), (4, 3)   -- genius has all roles
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

-- 계층형 게시글 테스트 데이터
INSERT INTO ARTICLES (ID, P_ID, REFERENCE, STEP, LEVEL, SUBJECT, AUTHOR, STATUS, HIT)
VALUES (1, NULL, 1, 1, 1, 'Spring Boot 계층형 게시판 소개', 1, 1, 120),
       (2, 1, 1, 2, 2, 'RE: Spring Boot 계층형 게시판 소개', 2, 1, 45),
       (3, 2, 1, 3, 3, 'RE: RE: Spring Boot 계층형 게시판 소개', 3, 1, 25),
       (4, NULL, 4, 1, 1, 'MyBatis 고급 매핑 기법', 2, 1, 89),
       (5, 4, 4, 2, 2, 'RE: MyBatis 고급 매핑 기법', 4, 1, 33)
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- 게시글 내용 테스트 데이터
INSERT INTO ARTICLE_CONTENT (ID, ARTICLE_ID, CONTENTS)
VALUES (1, 1, '이 글에서는 Spring Boot를 사용한 계층형 게시판 시스템의 구현 방법에 대해 설명합니다. 답글과 대댓글이 트리 구조로 표현되는 방식을 다룹니다.'),
       (2, 2, '매우 유용한 정보입니다. 특히 Step과 Level을 이용한 계층 구조 표현 방법이 인상적입니다.'),
       (3, 3, '저도 비슷한 프로젝트를 진행 중인데 많은 도움이 되었습니다. 감사합니다!'),
       (4, 4, 'MyBatis에서 복잡한 관계 매핑을 처리하는 다양한 기법들을 소개합니다. TypeHandler와 ResultMap을 활용한 고급 매핑 방법을 다룹니다.'),
       (5, 5, 'TypeHandler 부분이 특히 도움이 되었습니다. 실무에서 바로 적용해보겠습니다.')
ON DUPLICATE KEY UPDATE ID = VALUES(ID);

-- 계층형 댓글 테스트 데이터
INSERT INTO ARTICLE_COMMENT (ID, ARTICLE_ID, LEVEL, STEP, COMMENT, AUTHOR, STATUS)
VALUES (1, 1, 1, 1, '정말 좋은 글입니다. 계층형 구조 이해에 도움이 되었어요.', 3, 1),
       (2, 1, 2, 2, '저도 동감합니다. 실제 구현 예제가 있어서 더욱 좋네요.', 4, 1),
       (3, 1, 1, 3, '다음 글도 기대됩니다!', 2, 1),
       (4, 4, 1, 1, 'MyBatis TypeHandler 설명이 특히 유용했습니다.', 1, 1),
       (5, 4, 2, 2, '실무에서 바로 적용할 수 있을 것 같습니다.', 3, 1)
ON DUPLICATE KEY UPDATE ID = VALUES(ID);