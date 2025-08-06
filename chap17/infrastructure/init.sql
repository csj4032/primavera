-- ==============================================
-- Database: primavera - Chapter 17 Data Pipeline
-- ==============================================

-- 판매자 테이블
CREATE TABLE IF NOT EXISTS SELLERS
(
    ID              BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME            VARCHAR(100) NOT NULL,
    EMAIL           VARCHAR(100) UNIQUE NOT NULL,
    PHONE           VARCHAR(20),
    BUSINESS_NUMBER VARCHAR(20) UNIQUE,
    RATING          DECIMAL(3,2) DEFAULT 0.00 CHECK (RATING >= 0 AND RATING <= 5),
    CREATED_AT      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX IDX_SELLER_RATING (RATING),
    INDEX IDX_SELLER_EMAIL (EMAIL),
    INDEX IDX_SELLER_CREATED (CREATED_AT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '판매자 정보';

-- 카테고리 테이블 (계층구조)
CREATE TABLE IF NOT EXISTS CATEGORIES
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME        VARCHAR(100) NOT NULL,
    PARENT_ID   BIGINT,
    LEVEL       INT NOT NULL DEFAULT 1,
    PATH        VARCHAR(500),
    IS_ACTIVE   BOOLEAN DEFAULT TRUE,
    SORT_ORDER  INT DEFAULT 0,
    CREATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (PARENT_ID) REFERENCES CATEGORIES (ID) ON DELETE CASCADE,
    INDEX IDX_CATEGORY_PARENT (PARENT_ID),
    INDEX IDX_CATEGORY_LEVEL (LEVEL),
    INDEX IDX_CATEGORY_PATH (PATH),
    INDEX IDX_CATEGORY_ACTIVE (IS_ACTIVE),
    INDEX IDX_CATEGORY_SORT (SORT_ORDER)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '상품 카테고리';

-- 상품 테이블
CREATE TABLE IF NOT EXISTS PRODUCTS
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME        VARCHAR(200) NOT NULL,
    DESCRIPTION TEXT,
    PRICE       DECIMAL(12,2) NOT NULL CHECK (PRICE >= 0),
    STATUS      VARCHAR(20) DEFAULT 'ACTIVE',
    SELLER_ID   BIGINT NOT NULL,
    CATEGORY_ID BIGINT NOT NULL,
    STOCK       INT DEFAULT 0 CHECK (STOCK >= 0),
    VIEW_COUNT  BIGINT DEFAULT 0,
    CREATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (SELLER_ID) REFERENCES SELLERS (ID) ON DELETE RESTRICT,
    FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES (ID) ON DELETE RESTRICT,
    INDEX IDX_PRODUCT_NAME (NAME),
    INDEX IDX_PRODUCT_STATUS (STATUS),
    INDEX IDX_PRODUCT_SELLER (SELLER_ID),
    INDEX IDX_PRODUCT_CATEGORY (CATEGORY_ID),
    INDEX IDX_PRODUCT_PRICE (PRICE),
    INDEX IDX_PRODUCT_CREATED (CREATED_AT),
    INDEX IDX_PRODUCT_UPDATED (UPDATED_AT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '상품 정보';

-- 상품 태그 테이블 (검색 최적화)
CREATE TABLE IF NOT EXISTS PRODUCT_TAGS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    PRODUCT_ID BIGINT NOT NULL,
    TAG        VARCHAR(50) NOT NULL,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCTS (ID) ON DELETE CASCADE,
    UNIQUE KEY UNIQUE_PRODUCT_TAG (PRODUCT_ID, TAG),
    INDEX IDX_TAG (TAG),
    INDEX IDX_TAG_PRODUCT (PRODUCT_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '상품 태그';

-- 상품 가격 히스토리 (CDC 추적용)
CREATE TABLE IF NOT EXISTS PRODUCT_PRICE_HISTORY
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    PRODUCT_ID BIGINT NOT NULL,
    OLD_PRICE  DECIMAL(12,2),
    NEW_PRICE  DECIMAL(12,2) NOT NULL,
    CHANGED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHANGED_BY VARCHAR(100),
    FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCTS (ID) ON DELETE CASCADE,
    INDEX IDX_PRICE_HISTORY_PRODUCT (PRODUCT_ID),
    INDEX IDX_PRICE_HISTORY_DATE (CHANGED_AT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '상품 가격 변경 이력';

-- ==============================================
-- 초기 데이터 입력
-- ==============================================

-- 판매자 데이터
INSERT INTO SELLERS (ID, NAME, EMAIL, PHONE, BUSINESS_NUMBER, RATING) VALUES
(1, '테크스토어', 'contact@techstore.com', '02-1234-5678', '123-45-67890', 4.8),
(2, '가전마트', 'info@gadgetmart.com', '02-2345-6789', '234-56-78901', 4.5),
(3, '패션플러스', 'hello@fashionplus.com', '02-3456-7890', '345-67-89012', 4.3),
(4, '북스토리', 'books@bookstory.com', '02-4567-8901', '456-78-90123', 4.9),
(5, '스포츠샵', 'sports@sportsshop.com', '02-5678-9012', '567-89-01234', 4.2),
(6, '뷰티원', 'beauty@beautyone.com', '02-6789-0123', '678-90-12345', 4.7),
(7, '홈데코', 'home@homedeco.com', '02-7890-1234', '789-01-23456', 4.1),
(8, '펫프렌즈', 'pet@petfriends.com', '02-8901-2345', '890-12-34567', 4.6),
(9, '오토파츠', 'auto@autoparts.com', '02-9012-3456', '901-23-45678', 4.4),
(10, '키친웨어', 'kitchen@kitchenware.com', '02-0123-4567', '012-34-56789', 4.5)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 카테고리 데이터 (3단계 계층구조)
-- 1단계 카테고리
INSERT INTO CATEGORIES (ID, NAME, PARENT_ID, LEVEL, PATH, SORT_ORDER) VALUES
(1, '전자제품', NULL, 1, '전자제품', 1),
(2, '패션', NULL, 1, '패션', 2),
(3, '도서', NULL, 1, '도서', 3),
(4, '스포츠', NULL, 1, '스포츠', 4),
(5, '뷰티', NULL, 1, '뷰티', 5),
(6, '홈/인테리어', NULL, 1, '홈/인테리어', 6),
(7, '반려동물', NULL, 1, '반려동물', 7),
(8, '자동차', NULL, 1, '자동차', 8),
(9, '주방', NULL, 1, '주방', 9),
(10, '식품', NULL, 1, '식품', 10)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 2단계 카테고리
INSERT INTO CATEGORIES (ID, NAME, PARENT_ID, LEVEL, PATH, SORT_ORDER) VALUES
-- 전자제품 하위
(101, '컴퓨터', 1, 2, '전자제품 > 컴퓨터', 1),
(102, '모바일', 1, 2, '전자제품 > 모바일', 2),
(103, '가전', 1, 2, '전자제품 > 가전', 3),
-- 패션 하위
(201, '남성의류', 2, 2, '패션 > 남성의류', 1),
(202, '여성의류', 2, 2, '패션 > 여성의류', 2),
(203, '신발', 2, 2, '패션 > 신발', 3),
-- 도서 하위
(301, 'IT/프로그래밍', 3, 2, '도서 > IT/프로그래밍', 1),
(302, '소설', 3, 2, '도서 > 소설', 2),
(303, '자기계발', 3, 2, '도서 > 자기계발', 3)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 3단계 카테고리
INSERT INTO CATEGORIES (ID, NAME, PARENT_ID, LEVEL, PATH, SORT_ORDER) VALUES
-- 컴퓨터 하위
(1001, '노트북', 101, 3, '전자제품 > 컴퓨터 > 노트북', 1),
(1002, '데스크탑', 101, 3, '전자제품 > 컴퓨터 > 데스크탑', 2),
(1003, '모니터', 101, 3, '전자제품 > 컴퓨터 > 모니터', 3),
-- 모바일 하위
(1021, '스마트폰', 102, 3, '전자제품 > 모바일 > 스마트폰', 1),
(1022, '태블릿', 102, 3, '전자제품 > 모바일 > 태블릿', 2),
(1023, '스마트워치', 102, 3, '전자제품 > 모바일 > 스마트워치', 3)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 상품 데이터
INSERT INTO PRODUCTS (ID, NAME, DESCRIPTION, PRICE, STATUS, SELLER_ID, CATEGORY_ID, STOCK) VALUES
-- 전자제품
(1, '고성능 게이밍 노트북', '최신 RTX 4090 그래픽카드 탑재, Intel i9 프로세서', 3500000, 'ACTIVE', 1, 1001, 15),
(2, 'MacBook Pro 16인치', 'M3 Max 칩셋, 64GB RAM, 2TB SSD', 4800000, 'ACTIVE', 1, 1001, 8),
(3, '울트라와이드 모니터 49인치', '5120x1440 해상도, 240Hz, HDR1000', 1800000, 'ACTIVE', 1, 1003, 12),
(4, 'iPhone 15 Pro Max', '티타늄 디자인, A17 Pro 칩셋, 1TB', 2200000, 'ACTIVE', 2, 1021, 25),
(5, 'Galaxy S24 Ultra', '200MP 카메라, S-Pen 지원, 512GB', 1900000, 'ACTIVE', 2, 1021, 30),
(6, 'iPad Pro 12.9', 'M2 칩셋, 미니LED 디스플레이, 256GB', 1600000, 'ACTIVE', 2, 1022, 18),
(7, 'Apple Watch Ultra 2', '티타늄 케이스, 듀얼 주파수 GPS', 1100000, 'ACTIVE', 2, 1023, 22),

-- 패션
(8, '프리미엄 가죽 자켓', '이탈리아산 양가죽, 한정판 디자인', 890000, 'ACTIVE', 3, 201, 5),
(9, '캐시미어 코트', '100% 캐시미어, 핸드메이드 제작', 1200000, 'ACTIVE', 3, 202, 3),
(10, '런닝화 에어맥스', '에어쿠션, 플라이니트 소재', 189000, 'ACTIVE', 3, 203, 50),

-- 도서
(11, '클린 코드', '로버트 마틴의 애자일 소프트웨어 장인 정신', 33000, 'ACTIVE', 4, 301, 100),
(12, '이펙티브 자바 3판', '자바 프로그래밍 핵심 가이드', 36000, 'ACTIVE', 4, 301, 80),
(13, '마이크로서비스 패턴', '크리스 리처드슨의 마이크로서비스 설계', 42000, 'ACTIVE', 4, 301, 60),
(14, '도메인 주도 설계', '에릭 에반스의 DDD 바이블', 48000, 'ACTIVE', 4, 301, 40),
(15, '리팩터링 2판', '마틴 파울러의 코드 개선 기법', 35000, 'ACTIVE', 4, 301, 70),

-- 스포츠
(16, '프로 골프 클럽 세트', 'XXIO 12 풀세트, 남성용', 3200000, 'ACTIVE', 5, 4, 8),
(17, '로드 자전거', '카본 프레임, 시마노 105 구동계', 2800000, 'ACTIVE', 5, 4, 6),
(18, '홈트레이닝 덤벨 세트', '1kg-20kg 가변식, 거치대 포함', 480000, 'ACTIVE', 5, 4, 20),

-- 뷰티
(19, '안티에이징 세럼', '레티놀 2%, 비타민C 15% 함유', 89000, 'ACTIVE', 6, 5, 150),
(20, 'LED 마스크', '의료기기 인증, 630nm 파장', 380000, 'ACTIVE', 6, 5, 25),

-- 품절/비활성 상품
(21, '한정판 스니커즈', '콜라보레이션 한정 에디션', 890000, 'SOLD_OUT', 3, 203, 0),
(22, '단종 예정 노트북', '재고 정리 특가', 990000, 'DISCONTINUED', 1, 1001, 2)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 상품 태그 데이터
INSERT INTO PRODUCT_TAGS (PRODUCT_ID, TAG) VALUES
(1, '게이밍'), (1, '노트북'), (1, 'RTX4090'), (1, '고성능'),
(2, '맥북'), (2, '애플'), (2, 'M3'), (2, '프로'),
(3, '모니터'), (3, '울트라와이드'), (3, '게이밍'), (3, 'HDR'),
(4, '아이폰'), (4, '애플'), (4, '스마트폰'), (4, '프로맥스'),
(5, '갤럭시'), (5, '삼성'), (5, '스마트폰'), (5, '울트라'),
(11, '프로그래밍'), (11, '클린코드'), (11, 'IT도서'), (11, '베스트셀러'),
(12, '자바'), (12, '프로그래밍'), (12, 'IT도서'), (12, '이펙티브'),
(13, '마이크로서비스'), (13, '아키텍처'), (13, 'IT도서'), (13, '패턴'),
(14, 'DDD'), (14, '설계'), (14, 'IT도서'), (14, '도메인'),
(15, '리팩터링'), (15, '코드개선'), (15, 'IT도서'), (15, '마틴파울러')
ON DUPLICATE KEY UPDATE TAG = VALUES(TAG);

-- 초기 가격 히스토리 (CDC 테스트용)
INSERT INTO PRODUCT_PRICE_HISTORY (PRODUCT_ID, OLD_PRICE, NEW_PRICE, CHANGED_BY) VALUES
(1, 3800000, 3500000, 'SYSTEM'),
(4, 2400000, 2200000, 'SYSTEM'),
(11, 29000, 33000, 'SYSTEM')
ON DUPLICATE KEY UPDATE NEW_PRICE = VALUES(NEW_PRICE);

-- ==============================================
-- 통계 뷰 (Spring Batch 처리용)
-- ==============================================

CREATE OR REPLACE VIEW V_PRODUCT_SEARCH_INDEX AS
SELECT 
    p.ID as product_id,
    p.NAME as product_name,
    p.DESCRIPTION as product_description,
    p.PRICE as price,
    p.STATUS as status,
    p.STOCK as stock,
    p.VIEW_COUNT as view_count,
    s.ID as seller_id,
    s.NAME as seller_name,
    s.EMAIL as seller_email,
    s.RATING as seller_rating,
    c.ID as category_id,
    c.NAME as category_name,
    c.PATH as category_path,
    c.LEVEL as category_level,
    GROUP_CONCAT(DISTINCT pt.TAG ORDER BY pt.TAG SEPARATOR ',') as tags,
    CONCAT(p.NAME, ' ', IFNULL(p.DESCRIPTION, ''), ' ', 
           s.NAME, ' ', c.NAME, ' ', c.PATH, ' ',
           IFNULL(GROUP_CONCAT(DISTINCT pt.TAG SEPARATOR ' '), '')) as search_text,
    CASE 
        WHEN p.PRICE < 100000 THEN 'LOW'
        WHEN p.PRICE < 1000000 THEN 'MEDIUM'
        ELSE 'HIGH'
    END as price_range,
    p.CREATED_AT as created_at,
    p.UPDATED_AT as updated_at
FROM PRODUCTS p
INNER JOIN SELLERS s ON p.SELLER_ID = s.ID
INNER JOIN CATEGORIES c ON p.CATEGORY_ID = c.ID
LEFT JOIN PRODUCT_TAGS pt ON p.ID = pt.PRODUCT_ID
GROUP BY p.ID, p.NAME, p.DESCRIPTION, p.PRICE, p.STATUS, p.STOCK, p.VIEW_COUNT,
         s.ID, s.NAME, s.EMAIL, s.RATING,
         c.ID, c.NAME, c.PATH, c.LEVEL,
         p.CREATED_AT, p.UPDATED_AT;

-- ==============================================
-- Debezium CDC를 위한 권한 설정 (필요시)
-- ==============================================
-- GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'primavera'@'%';
-- FLUSH PRIVILEGES;