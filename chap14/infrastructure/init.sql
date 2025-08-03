-- ==============================================
-- Database: primavera (chap14)
-- Advanced JPA Application with Auditing
-- ==============================================

-- 사용자 테이블 (User 엔티티)
CREATE TABLE IF NOT EXISTS USER
(
    ID            BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL         VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD      VARCHAR(255)        NOT NULL,
    NICKNAME      VARCHAR(50)         NOT NULL,
    STATUS        INT DEFAULT 1,
    CONNECTION_ID BIGINT,
    CREATED_AT    DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_EMAIL (EMAIL),
    INDEX IDX_STATUS (STATUS),
    INDEX IDX_CONNECTION_ID (CONNECTION_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 역할 테이블 (Role 엔티티)
CREATE TABLE IF NOT EXISTS ROLE
(
    ID   BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE INT NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자-역할 연결 테이블 (ManyToMany)
CREATE TABLE IF NOT EXISTS USER_ROLE
(
    ID      BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID BIGINT NOT NULL,
    ROLE_ID BIGINT NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES USER (ID) ON DELETE CASCADE,
    FOREIGN KEY (ROLE_ID) REFERENCES ROLE (ID) ON DELETE CASCADE,
    UNIQUE KEY UNIQUE_USER_ROLE (USER_ID, ROLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자 소셜 연결 테이블 (UserConnection 엔티티)
CREATE TABLE IF NOT EXISTS USER_CONNECTION
(
    ID           BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL        VARCHAR(100),
    PROVIDER     INT,
    PROVIDER_ID  VARCHAR(255),
    DISPLAY_NAME VARCHAR(100),
    PROFILE_URL  VARCHAR(500),
    IMAGE_URL    VARCHAR(500),
    ACCESS_TOKEN VARCHAR(1000),
    EXPIRE_TIME  BIGINT,
    CREATED_AT   DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_PROVIDER_ID (PROVIDER_ID),
    INDEX IDX_EMAIL (EMAIL)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시글 컨텐트 테이블 (Content 엔티티)
CREATE TABLE IF NOT EXISTS ARTICLE_CONTENT
(
    ID       BIGINT AUTO_INCREMENT PRIMARY KEY,
    CONTENTS TEXT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시글 테이블 (Article 엔티티)
CREATE TABLE IF NOT EXISTS ARTICLE
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    P_ID       BIGINT DEFAULT 0,
    REFERENCE  BIGINT DEFAULT 0,
    STEP       INT    DEFAULT 1,
    LEVEL      INT    DEFAULT 1,
    STATUS     INT    DEFAULT 1,
    SUBJECT    VARCHAR(200) NOT NULL,
    AUTHOR     BIGINT       NOT NULL,
    HIT        INT    DEFAULT 0,
    RECOMMEND  INT    DEFAULT 0,
    DISAPPROVE INT    DEFAULT 0,
    CONTENT_ID BIGINT,
    CREATED_AT DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (AUTHOR) REFERENCES USER (ID) ON DELETE CASCADE,
    FOREIGN KEY (CONTENT_ID) REFERENCES ARTICLE_CONTENT (ID) ON DELETE CASCADE,
    INDEX IDX_AUTHOR (AUTHOR),
    INDEX IDX_STATUS (STATUS),
    INDEX IDX_REFERENCE (REFERENCE),
    INDEX IDX_STEP (STEP),
    INDEX IDX_LEVEL (LEVEL),
    INDEX IDX_CREATED_AT (CREATED_AT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시글 댓글 테이블 (Comment 엔티티)
CREATE TABLE IF NOT EXISTS ARTICLE_COMMENT
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID BIGINT       NOT NULL,
    LEVEL      INT    DEFAULT 1,
    STEP       INT    DEFAULT 1,
    AUTHOR     BIGINT       NOT NULL,
    COMMENT    TEXT         NOT NULL,
    STATUS     INT    DEFAULT 1,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID) ON DELETE CASCADE,
    FOREIGN KEY (AUTHOR) REFERENCES USER (ID) ON DELETE CASCADE,
    INDEX IDX_ARTICLE_ID (ARTICLE_ID),
    INDEX IDX_AUTHOR (AUTHOR),
    INDEX IDX_STATUS (STATUS),
    INDEX IDX_CREATED_AT (CREATED_AT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시글 첨부파일 테이블 (Attachment 엔티티)
CREATE TABLE IF NOT EXISTS ARTICLE_ATTACHMENT
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID BIGINT       NOT NULL,
    NAME       VARCHAR(255) NOT NULL,
    SIZE       BIGINT DEFAULT 0,
    PATH       VARCHAR(500),
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID) ON DELETE CASCADE,
    INDEX IDX_ARTICLE_ID (ARTICLE_ID),
    INDEX IDX_NAME (NAME)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Hibernate Envers 감사 테이블들
-- 사용자 감사 테이블
CREATE TABLE IF NOT EXISTS USER_AUD
(
    ID            BIGINT       NOT NULL,
    REV           INT          NOT NULL,
    REVTYPE       TINYINT,
    EMAIL         VARCHAR(100),
    PASSWORD      VARCHAR(255),
    NICKNAME      VARCHAR(50),
    STATUS        INT,
    CONNECTION_ID BIGINT,
    CREATED_AT    DATETIME,
    UPDATED_AT    DATETIME,
    PRIMARY KEY (ID, REV)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 리비전 정보 테이블
CREATE TABLE IF NOT EXISTS REVINFO
(
    REV      INT AUTO_INCREMENT PRIMARY KEY,
    REVTSTMP BIGINT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 외래키 제약조건 추가
ALTER TABLE USER ADD CONSTRAINT FK_USER_CONNECTION 
    FOREIGN KEY (CONNECTION_ID) REFERENCES USER_CONNECTION (ID) ON DELETE SET NULL;

-- 기본 역할 데이터
INSERT INTO ROLE (ID, TYPE)
VALUES (1, 1), -- ADMINISTRATOR
       (2, 2), -- MANAGER  
       (3, 3)  -- USER
ON DUPLICATE KEY UPDATE TYPE = VALUES(TYPE);

-- 기본 사용자 데이터
INSERT INTO USER (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'admin@primavera.com', '{noop}admin123', 'Administrator', 1),
       (2, 'manager@primavera.com', '{noop}manager123', 'Manager', 1),
       (3, 'user@primavera.com', '{noop}user123', 'User', 1),
       (4, 'genius@primavera.com', '{noop}test', 'Genius', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 사용자-역할 매핑
INSERT INTO USER_ROLE (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), (1, 3), -- admin has all roles
       (2, 2), (2, 3),          -- manager has manager and user roles
       (3, 3),                  -- user has user role
       (4, 1), (4, 2), (4, 3)   -- genius has all roles
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

-- 샘플 게시글 컨텐트
INSERT INTO ARTICLE_CONTENT (ID, CONTENTS)
VALUES (1, '첫 번째 게시글의 내용입니다.'),
       (2, '두 번째 게시글의 내용입니다.'),
       (3, '세 번째 게시글의 내용입니다.'),
       (4, '네 번째 게시글의 내용입니다.'),
       (5, '다섯 번째 게시글의 내용입니다.'),
       (6, '여섯 번째 게시글의 내용입니다.'),
       (7, '일곱 번째 게시글의 내용입니다.'),
       (8, '여덟 번째 게시글의 내용입니다.'),
       (9, '아홉 번째 게시글의 내용입니다.'),
       (10, '열 번째 게시글의 내용입니다.'),
       (11, '열한 번째 게시글의 내용입니다.'),
       (12, '열두 번째 게시글의 내용입니다.'),
       (13, '열세 번째 게시글의 내용입니다.'),
       (14, '열네 번째 게시글의 내용입니다.'),
       (15, '열다섯 번째 게시글의 내용입니다.'),
       (16, '열여섯 번째 게시글의 내용입니다.'),
       (17, '열일곱 번째 게시글의 내용입니다.'),
       (18, '열여덟 번째 게시글의 내용입니다.'),
       (19, '열아홉 번째 게시글의 내용입니다.'),
       (20, '스무 번째 게시글의 내용입니다.'),
       (21, '스물한 번째 게시글의 내용입니다.'),
       (22, '스물두 번째 게시글의 내용입니다.'),
       (23, '스물세 번째 게시글의 내용입니다.'),
       (24, '스물네 번째 게시글의 내용입니다.')
ON DUPLICATE KEY UPDATE CONTENTS = VALUES(CONTENTS);

-- 샘플 게시글 데이터 (계층형 구조)
INSERT INTO ARTICLE (ID, P_ID, REFERENCE, STEP, LEVEL, STATUS, SUBJECT, AUTHOR, HIT, RECOMMEND, DISAPPROVE, CONTENT_ID)
VALUES 
-- 원글들
(1, 0, 1, 1, 1, 1, 'JPA 기본 개념', 1, 10, 5, 0, 1),
(2, 0, 2, 1, 1, 1, 'Spring Data JPA 활용', 2, 15, 8, 1, 2),
(3, 0, 3, 1, 1, 1, 'Hibernate 고급 기능', 3, 20, 12, 2, 3),
(4, 0, 4, 1, 1, 1, 'JPA 성능 최적화', 4, 25, 15, 3, 4),
(5, 0, 5, 1, 1, 1, 'Entity 관계 매핑', 1, 18, 10, 1, 5),

-- 첫 번째 원글의 답글들
(6, 1, 1, 2, 2, 1, 'Re: JPA 기본 개념 - EntityManager', 2, 5, 2, 0, 6),
(7, 1, 1, 3, 2, 1, 'Re: JPA 기본 개념 - 영속성 컨텍스트', 3, 8, 4, 1, 7),
(8, 6, 1, 4, 3, 1, 'Re: EntityManager 생명주기', 4, 3, 1, 0, 8),

-- 두 번째 원글의 답글들
(9, 2, 2, 2, 2, 1, 'Re: Spring Data JPA - Repository', 1, 7, 3, 0, 9),
(10, 2, 2, 3, 2, 1, 'Re: Spring Data JPA - Query Methods', 4, 12, 6, 2, 10),

-- 세 번째 원글의 답글들
(11, 3, 3, 2, 2, 1, 'Re: Hibernate - N+1 문제', 2, 15, 8, 1, 11),
(12, 3, 3, 3, 2, 1, 'Re: Hibernate - Lazy Loading', 1, 10, 5, 0, 12),
(13, 11, 3, 4, 3, 1, 'Re: N+1 해결 방법', 3, 6, 2, 0, 13),

-- 네 번째 원글의 답글들
(14, 4, 4, 2, 2, 1, 'Re: JPA 성능 - 배치 처리', 3, 9, 4, 1, 14),
(15, 4, 4, 3, 2, 1, 'Re: JPA 성능 - 캐시 전략', 2, 11, 7, 0, 15),

-- 다섯 번째 원글의 답글들
(16, 5, 5, 2, 2, 1, 'Re: Entity 관계 - OneToMany', 4, 8, 3, 0, 16),
(17, 5, 5, 3, 2, 1, 'Re: Entity 관계 - ManyToMany', 1, 13, 9, 1, 17),
(18, 16, 5, 4, 3, 1, 'Re: OneToMany 주의사항', 2, 4, 1, 0, 18),

-- 추가 원글들
(19, 0, 19, 1, 1, 1, 'JPA Auditing 기능', 3, 22, 14, 2, 19),
(20, 0, 20, 1, 1, 1, 'JPA 트랜잭션 관리', 4, 30, 18, 3, 20),
(21, 19, 19, 2, 2, 1, 'Re: Auditing - @CreatedDate', 1, 6, 2, 0, 21),
(22, 19, 19, 3, 2, 1, 'Re: Auditing - @LastModifiedBy', 2, 9, 5, 1, 22),
(23, 20, 20, 2, 2, 1, 'Re: 트랜잭션 - @Transactional', 3, 12, 8, 0, 23),
(24, 20, 20, 3, 2, 1, 'Re: 트랜잭션 - 격리 수준', 4, 15, 10, 2, 24)
ON DUPLICATE KEY UPDATE SUBJECT = VALUES(SUBJECT);

-- 샘플 댓글 데이터
INSERT INTO ARTICLE_COMMENT (ID, ARTICLE_ID, LEVEL, STEP, AUTHOR, COMMENT, STATUS)
VALUES 
(1, 1, 1, 1, 2, '좋은 설명 감사합니다!', 1),
(2, 1, 1, 2, 3, 'JPA 공부하는데 많은 도움이 되었어요.', 1),
(3, 2, 1, 1, 4, 'Spring Data JPA 관련 추가 자료가 있을까요?', 1),
(4, 3, 1, 1, 1, 'N+1 문제 해결 방법이 궁금합니다.', 1),
(5, 4, 1, 1, 2, '성능 최적화에 대해 더 자세히 알고 싶어요.', 1)
ON DUPLICATE KEY UPDATE COMMENT = VALUES(COMMENT);