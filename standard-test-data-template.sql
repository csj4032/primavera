-- ==============================================
-- Standardized Test Data Template for chap04-chap14
-- 일관성 있는 테스트 데이터 표준
-- ==============================================

-- 권한 테이블 (ROLES 테이블이 있는 챕터용)
INSERT INTO ROLES (ID, TYPE)
VALUES (1, 1), -- ADMINISTRATOR
       (2, 2), -- MANAGER  
       (3, 3)  -- USER
ON DUPLICATE KEY UPDATE TYPE = VALUES(TYPE);

-- 권한 테이블 (NAME, DESCRIPTION 포함하는 챕터용 - chap07 등)
-- INSERT INTO ROLES (ID, NAME, DESCRIPTION, TYPE)
-- VALUES (1, 'ROLE_ADMINISTRATOR', '최고 관리자', 1),
--        (2, 'ROLE_MANAGER', '관리자', 2),
--        (3, 'ROLE_USER', '일반 사용자', 3)
-- ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 사용자 테이블 표준 데이터
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT, UPDATED_AT)
VALUES (1, 'genius@primavera.com', '{noop}test', 'Genius', 1, NOW(), NOW()),
       (2, 'admin@primavera.com', '{noop}test', 'Admin', 1, NOW(), NOW()),
       (3, 'user@primavera.com', '{noop}test', 'User', 1, NOW(), NOW()),
       (4, 'son@primavera.com', '{noop}test', 'Son', 1, NOW(), NOW()),
       (5, 'messi@primavera.com', '{noop}test', 'Messi', 1, NOW(), NOW()),
       (6, 'ronaldo@primavera.com', '{noop}test', 'Ronaldo', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 사용자-권한 매핑 표준 데이터
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

-- ==============================================
-- 매핑 설명:
-- User ID 1 (genius): 모든 권한 (ADMINISTRATOR, MANAGER, USER)
-- User ID 2 (admin): ADMINISTRATOR, MANAGER 권한
-- User ID 3 (user): USER 권한만
-- User ID 4-6 (son, messi, ronaldo): USER 권한만
-- ==============================================