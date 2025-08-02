-- 초기 데이터 삽입 (H2 호환)
INSERT INTO ROLES (ID, NAME, DESCRIPTION, TYPE)
VALUES (1, 'ROLE_ADMIN', '테스트 관리자', 1),
       (2, 'ROLE_MANAGER', '테스트 매니저', 2),
       (3, 'ROLE_USER', '테스트 사용자', 3);

INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS)
VALUES (1, 'genius@primavera.com', '{noop}test', 'Genius', 1),
       (2, 'admin@primavera.com', '{noop}test', 'Admin', 1),
       (3, 'user@primavera.com', '{noop}test', 'User', 1),
       (4, 'son@primavera.com', '{noop}test', 'Son', 1),
       (5, 'messi@primavera.com', '{noop}test', 'Messi', 1),
       (6, 'ronaldo@primavera.com', '{noop}test', 'Ronaldo', 1);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 1), (1, 2), (1, 3), -- genius -> all roles
       (2, 1), (2, 2),         -- admin -> admin, manager
       (3, 3),                 -- user -> user
       (4, 3), (5, 3), (6, 3); -- sports players -> user

-- YEAR 컬럼을 큰따옴표로 감싸서 INSERT
INSERT INTO WINNERS (ID, NAME, "YEAR", SPORT, PRIZE, AMOUNT)
VALUES (1, 'Lionel Messi', 2023, 'Football', 'Ballon d''Or', 1000000.00),
       (2, 'Erling Haaland', 2023, 'Football', 'Golden Boot', 500000.00),
       (3, 'Lewis Hamilton', 2023, 'Formula 1', 'World Championship', 2000000.00),
       (4, 'Serena Williams', 2023, 'Tennis', 'Wimbledon', 750000.00),
       (5, 'Tiger Woods', 2023, 'Golf', 'Masters Tournament', 1500000.00);