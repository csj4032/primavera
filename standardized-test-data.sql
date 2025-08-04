-- ==============================================
-- Standardized Test Data for chap04-chap14
-- 일관성 있는 테스트 데이터 표준
-- ==============================================

-- 권한 테이블 표준 데이터
INSERT INTO ROLES (ID, TYPE)
VALUES (1, 1), -- ADMINISTRATOR
       (2, 2), -- MANAGER  
       (3, 3)  -- USER
ON DUPLICATE KEY UPDATE TYPE = VALUES(TYPE);

-- 사용자 테이블 표준 데이터
-- 비밀번호: {noop}test (테스트용 평문)
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT, UPDATED_AT)
VALUES (1, 'admin@primavera.com', '{noop}test', 'Administrator', 1, NOW(), NOW()),
       (2, 'manager@primavera.com', '{noop}test', 'Manager', 1, NOW(), NOW()),
       (3, 'user@primavera.com', '{noop}test', 'User', 1, NOW(), NOW()),
       (4, 'tester@primavera.com', '{noop}test', 'Tester', 1, NOW(), NOW()),
       (5, 'genius@primavera.com', '{noop}test', 'Genius', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

-- 사용자-권한 매핑 표준 데이터
INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), (1, 3),  -- admin has all roles (ADMINISTRATOR, MANAGER, USER)
       (2, 2), (2, 3),           -- manager has MANAGER and USER roles
       (3, 3),                   -- user has USER role only
       (4, 3),                   -- tester has USER role
       (5, 1), (5, 2), (5, 3)    -- genius has all roles (ADMINISTRATOR, MANAGER, USER)
ON DUPLICATE KEY UPDATE USER_ID = VALUES(USER_ID);

-- ==============================================
-- 설명:
-- 1. ROLES: ID와 TYPE만 포함 (심플한 구조)
--    - 1: ADMINISTRATOR (TYPE=1)
--    - 2: MANAGER (TYPE=2)
--    - 3: USER (TYPE=3)
--
-- 2. USERS: 5명의 표준 사용자
--    - admin: 모든 권한 보유
--    - manager: 매니저 + 사용자 권한
--    - user: 일반 사용자 권한만
--    - tester: 테스트용 일반 사용자
--    - genius: 모든 권한 보유 (개발자)
--
-- 3. 비밀번호: {noop}test 통일 (Spring Security NoOp 인코딩)
-- 4. STATUS: 1 (활성 상태)
-- ==============================================