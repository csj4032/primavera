MERGE INTO ROLES (ID, NAME, DESCRIPTION, TYPE) KEY (ID)
    VALUES (1, 'ROLE_ADMIN', '테스트 관리자', 1),
    (2, 'ROLE_USER', '테스트 사용자', 3);


MERGE INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS) KEY (ID)
    VALUES (1, 'genius@primavera.com', '{noop}test', 'Genius', 'ACTIVE'),
    (2, 'son@primavera.com', '{noop}test', 'Son', 'ACTIVE'),
    (3, 'messi@primavera.com', '{noop}test', 'Messi', 'ACTIVE'),
    (4, 'ronaldo@primavera.com', '{noop}test', 'Ronaldo', 'ACTIVE');


MERGE INTO USER_ROLES (USER_ID, ROLE_ID) KEY (USER_ID, ROLE_ID)
    VALUES (1, 1), (1, 2), -- genius -> admin, user
    (2, 2), -- son -> user
    (3, 2), -- messi -> user
    (4, 2); -- ronaldo -> user