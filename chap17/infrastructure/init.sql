-- ==============================================
-- Database: primavera (Chapter 17: Data Pipeline)
-- ==============================================

-- 카테고리 테이블
CREATE TABLE IF NOT EXISTS CATEGORIES (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(100) NOT NULL,
    LEVEL INT DEFAULT 1,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX IDX_CATEGORY_NAME (NAME),
    INDEX IDX_CATEGORY_LEVEL (LEVEL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 판매자 테이블
CREATE TABLE IF NOT EXISTS SELLERS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(100) NOT NULL,
    EMAIL VARCHAR(255) UNIQUE NOT NULL,
    RATING DECIMAL(3,2) DEFAULT 0.00,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX IDX_SELLER_EMAIL (EMAIL),
    INDEX IDX_SELLER_RATING (RATING)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 상품 테이블
CREATE TABLE IF NOT EXISTS PRODUCTS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(200) NOT NULL,
    DESCRIPTION TEXT,
    PRICE INT NOT NULL,
    STATUS VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    SELLER_ID BIGINT NOT NULL,
    CATEGORY_ID BIGINT NOT NULL,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (SELLER_ID) REFERENCES SELLERS(ID) ON DELETE CASCADE,
    FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES(ID) ON DELETE CASCADE,
    
    INDEX IDX_PRODUCT_NAME (NAME),
    INDEX IDX_PRODUCT_PRICE (PRICE),
    INDEX IDX_PRODUCT_STATUS (STATUS),
    INDEX IDX_PRODUCT_SELLER (SELLER_ID),
    INDEX IDX_PRODUCT_CATEGORY (CATEGORY_ID),
    INDEX IDX_PRODUCT_UPDATED (UPDATED_AT),
    FULLTEXT INDEX FULLTEXT_PRODUCT_NAME_DESC (NAME, DESCRIPTION)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기본 카테고리 데이터
INSERT INTO CATEGORIES (ID, NAME, LEVEL) VALUES
(1, '전자제품', 1),
(2, '컴퓨터', 2),
(3, '스마트폰', 2),
(4, '가전제품', 1),
(5, '냉장고', 2),
(6, '세탁기', 2),
(7, '의류', 1),
(8, '남성복', 2),
(9, '여성복', 2),
(10, '도서', 1)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 기본 판매자 데이터
INSERT INTO SELLERS (ID, NAME, EMAIL, RATING) VALUES
(1, '테크스토어', 'techstore@primavera.com', 4.8),
(2, '디지털마트', 'digital@primavera.com', 4.5),
(3, '홈앤라이프', 'homelife@primavera.com', 4.2),
(4, '패션하우스', 'fashion@primavera.com', 4.6),
(5, '북스토어', 'bookstore@primavera.com', 4.9),
(6, '엘렉트로닉스', 'electronics@primavera.com', 4.3),
(7, '스마트샵', 'smartshop@primavera.com', 4.7),
(8, '라이프스타일', 'lifestyle@primavera.com', 4.1),
(9, '프리미엄몰', 'premium@primavera.com', 4.8),
(10, '글로벌스토어', 'global@primavera.com', 4.4)
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 샘플 상품 데이터 (대용량 데이터를 위한 다양한 제품)
INSERT INTO PRODUCTS (ID, NAME, DESCRIPTION, PRICE, STATUS, SELLER_ID, CATEGORY_ID) VALUES
-- 전자제품/컴퓨터
(1, 'MacBook Pro 16인치 M3 Pro', 'Apple의 최신 MacBook Pro 16인치 모델로 M3 Pro 칩셋을 탑재했습니다.', 3200000, 'ACTIVE', 1, 2),
(2, 'Dell XPS 13 Plus', '13인치 울트라북으로 최신 Intel 12세대 프로세서 탑재', 1800000, 'ACTIVE', 2, 2),
(3, 'ThinkPad X1 Carbon', 'Lenovo의 프리미엄 비즈니스 노트북', 2100000, 'ACTIVE', 1, 2),
(4, 'Samsung Galaxy Book3 Pro', '삼성의 프리미엄 노트북 시리즈', 1650000, 'ACTIVE', 6, 2),
(5, 'LG gram 17인치', '초경량 17인치 노트북', 1900000, 'ACTIVE', 7, 2),

-- 전자제품/스마트폰
(6, 'iPhone 15 Pro Max 256GB', 'Apple의 최신 플래그십 스마트폰', 1650000, 'ACTIVE', 1, 3),
(7, 'Samsung Galaxy S24 Ultra', '삼성의 최고급 스마트폰', 1580000, 'ACTIVE', 6, 3),
(8, 'Google Pixel 8 Pro', '구글의 프리미엄 스마트폰', 1200000, 'ACTIVE', 7, 3),
(9, 'Xiaomi 14 Pro', '샤오미의 플래그십 모델', 890000, 'ACTIVE', 8, 3),
(10, 'OnePlus 12', '원플러스의 최신 프리미엄 모델', 950000, 'ACTIVE', 9, 3),

-- 가전제품/냉장고
(11, 'LG 디오스 오브제컬렉션 냉장고', 'LG의 프리미엄 냉장고 시리즈', 2800000, 'ACTIVE', 3, 5),
(12, 'Samsung 비스포크 냉장고 4도어', '삼성의 맞춤형 냉장고', 2400000, 'ACTIVE', 3, 5),
(13, 'Whirlpool 양문형 냉장고', '월풀의 대용량 냉장고', 1650000, 'ACTIVE', 3, 5),

-- 가전제품/세탁기
(14, 'LG 트롬 AI DD 세탁기 21kg', 'LG의 AI 기술이 적용된 대용량 세탁기', 1890000, 'ACTIVE', 3, 6),
(15, 'Samsung 그랑데AI 세탁기 20kg', '삼성의 AI 세탁기', 1750000, 'ACTIVE', 3, 6),
(16, 'Miele W1 프론트로드 세탁기', '독일 프리미엄 브랜드 세탁기', 3200000, 'ACTIVE', 3, 6),

-- 의류/남성복
(17, 'Hugo Boss 정장 슈트', '휴고보스의 프리미엄 정장', 980000, 'ACTIVE', 4, 8),
(18, 'Ralph Lauren 폴로 셔츠', '랄프로렌의 클래식 폴로 셔츠', 180000, 'ACTIVE', 4, 8),
(19, 'Tommy Hilfiger 캐주얼 재킷', '토미힐피거의 캐주얼 재킷', 320000, 'ACTIVE', 4, 8),

-- 의류/여성복
(20, 'Chanel 트위드 재킷', '샤넬의 시그니처 트위드 재킷', 5800000, 'ACTIVE', 4, 9),
(21, 'Zara 원피스 컬렉션', '자라의 트렌디한 원피스', 89000, 'ACTIVE', 4, 9),
(22, 'Uniqlo 캐시미어 니트', '유니클로의 프리미엄 캐시미어', 129000, 'ACTIVE', 4, 9),

-- 도서
(23, '데이터 중심 애플리케이션 설계', '마틴 클레프만 저, 시스템 설계의 바이블', 45000, 'ACTIVE', 5, 10),
(24, '클린 아키텍처', '로버트 C. 마틴의 소프트웨어 설계 원칙', 32000, 'ACTIVE', 5, 10),
(25, 'Effective Java 3판', '조슈아 블로크의 자바 프로그래밍 가이드', 38000, 'ACTIVE', 5, 10),

-- 추가 전자제품
(26, 'iPad Pro 12.9인치 M4', 'Apple의 최신 iPad Pro', 1590000, 'ACTIVE', 1, 1),
(27, 'Sony WH-1000XM5 헤드폰', '소니의 노이즈 캔슬링 헤드폰', 450000, 'ACTIVE', 2, 1),
(28, 'Nintendo Switch OLED', '닌텐도의 최신 게임 콘솔', 380000, 'ACTIVE', 2, 1),
(29, 'Apple Watch Ultra 2', 'Apple의 프리미엄 스마트워치', 980000, 'ACTIVE', 1, 1),
(30, 'Tesla Model Y 전기차', '테슬라의 중형 SUV 전기차', 65000000, 'ACTIVE', 9, 1),

-- 다양한 가격대의 제품들
(31, 'Dyson V15 무선청소기', '다이슨의 프리미엄 무선청소기', 890000, 'ACTIVE', 3, 4),
(32, '삼성 QLED 8K TV 75인치', '삼성의 프리미엄 8K TV', 4500000, 'ACTIVE', 6, 1),
(33, 'Bose QuietComfort Earbuds', '보스의 노이즈 캔슬링 이어버드', 350000, 'ACTIVE', 2, 1),
(34, 'Microsoft Surface Pro 9', '마이크로소프트의 2in1 태블릿', 1380000, 'ACTIVE', 7, 2),
(35, '로지텍 MX Master 3S 마우스', '로지텍의 프리미엄 무선 마우스', 125000, 'ACTIVE', 8, 2),

-- 저가 제품들
(36, '다이소 USB 케이블', '다양한 기기 호환 USB 케이블', 3000, 'ACTIVE', 10, 1),
(37, '노브랜드 블루투스 이어폰', '저렴한 무선 이어폰', 25000, 'ACTIVE', 10, 1),
(38, '기본 후드티', '베이직한 후드티', 35000, 'ACTIVE', 8, 7),
(39, '스마트폰 케이스', '범용 스마트폰 보호 케이스', 12000, 'ACTIVE', 10, 3),
(40, '충전 어댑터', '5V 2A USB 충전기', 15000, 'ACTIVE', 10, 1)

ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

-- 인덱스 최적화를 위한 추가 설정
ANALYZE TABLE PRODUCTS;
ANALYZE TABLE SELLERS;
ANALYZE TABLE CATEGORIES;