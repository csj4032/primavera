-- ==============================================
-- Primavera Chapter 18 - Microservices Environment Database
-- 마이크로서비스 아키텍처, 분산 시스템, 서비스 메시
-- MariaDB 11.4.7 - 마이크로서비스 운영 환경
-- ==============================================

-- 기본 데이터베이스 설정
ALTER DATABASE primavera CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==============================================
-- 마이크로서비스 데이터베이스 생성
-- ==============================================

-- 1. 마이크로서비스 통합 데이터베이스
CREATE DATABASE IF NOT EXISTS primavera_microservices CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==============================================
-- 권한 설정
-- ==============================================

GRANT ALL PRIVILEGES ON primavera.* TO 'primavera'@'%' IDENTIFIED BY 'primavera';
GRANT ALL PRIVILEGES ON primavera_microservices.* TO 'primavera'@'%' IDENTIFIED BY 'primavera';

FLUSH PRIVILEGES;

-- ==============================================
-- 마이크로서비스 데이터베이스 (primavera_microservices)
-- Chapter 18용 분산 시스템 및 마이크로서비스 테이블
-- ==============================================

USE primavera_microservices;

-- ==============================================
-- 공통 서비스 테이블들
-- ==============================================

-- 사용자 서비스 (User Service) - 중앙 인증 시스템
CREATE TABLE IF NOT EXISTS users
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    email               VARCHAR(100) UNIQUE NOT NULL,
    password            VARCHAR(255),
    nickname            VARCHAR(50)         NOT NULL,
    first_name          VARCHAR(50),
    last_name           VARCHAR(50),
    phone               VARCHAR(20),
    profile_image_url   VARCHAR(500),
    provider            VARCHAR(20) DEFAULT 'LOCAL', -- LOCAL, GOOGLE, FACEBOOK, GITHUB, KAKAO
    provider_id         VARCHAR(100),
    email_verified      BOOLEAN     DEFAULT FALSE,
    phone_verified      BOOLEAN     DEFAULT FALSE,
    two_factor_enabled  BOOLEAN     DEFAULT FALSE,
    preferences         JSON,        -- 사용자 설정
    metadata            JSON,        -- 확장 메타데이터
    status              VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, LOCKED, BANNED
    last_login_at       DATETIME,
    last_login_ip       VARCHAR(45),
    password_changed_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    created_at          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version             BIGINT      DEFAULT 0, -- Optimistic Locking
    
    INDEX idx_users_email (email),
    INDEX idx_users_nickname (nickname),
    INDEX idx_users_provider (provider, provider_id),
    INDEX idx_users_status (status),
    INDEX idx_users_created_at (created_at)
);

-- 역할 및 권한 관리 (RBAC)
CREATE TABLE IF NOT EXISTS roles
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    description VARCHAR(200),
    service_name VARCHAR(50),        -- 마이크로서비스 이름
    permissions JSON,                -- 권한 목록 (JSON 배열)
    is_system   BOOLEAN     DEFAULT FALSE,
    status      VARCHAR(20) DEFAULT 'ACTIVE',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_roles_service_name (service_name),
    INDEX idx_roles_status (status)
);

-- 사용자 역할 매핑
CREATE TABLE IF NOT EXISTS user_roles
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT   NOT NULL,
    role_id    BIGINT   NOT NULL,
    granted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT,
    expires_at DATETIME,
    is_active  BOOLEAN  DEFAULT TRUE,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_roles_user_id (user_id),
    INDEX idx_user_roles_role_id (role_id)
);

-- API 키 관리 (서비스 간 인증)
CREATE TABLE IF NOT EXISTS api_keys
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_id       VARCHAR(50) UNIQUE NOT NULL,
    key_hash     VARCHAR(255)       NOT NULL, -- API 키 해시
    service_name VARCHAR(50)        NOT NULL,
    description  VARCHAR(200),
    permissions  JSON,               -- 허용된 권한 목록
    rate_limit   INT DEFAULT 1000,  -- 분당 요청 제한
    ip_whitelist JSON,              -- 허용 IP 목록
    is_active    BOOLEAN     DEFAULT TRUE,
    expires_at   DATETIME,
    last_used_at DATETIME,
    created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    created_by   BIGINT,
    
    FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_api_keys_service_name (service_name),
    INDEX idx_api_keys_is_active (is_active)
);

-- ==============================================
-- 상품 서비스 (Product Service)
-- ==============================================

-- 카테고리 테이블
CREATE TABLE IF NOT EXISTS categories
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    slug         VARCHAR(100) UNIQUE NOT NULL,
    description  TEXT,
    parent_id    BIGINT,
    level        INT DEFAULT 1,
    sort_order   INT DEFAULT 0,
    image_url    VARCHAR(500),
    seo_title    VARCHAR(200),
    seo_description VARCHAR(500),
    seo_keywords VARCHAR(200),
    is_featured  BOOLEAN     DEFAULT FALSE,
    status       VARCHAR(20) DEFAULT 'ACTIVE',
    created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE SET NULL,
    INDEX idx_categories_parent_id (parent_id),
    INDEX idx_categories_slug (slug),
    INDEX idx_categories_status (status),
    INDEX idx_categories_sort_order (sort_order)
);

-- 브랜드 테이블
CREATE TABLE IF NOT EXISTS brands
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    slug        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    logo_url    VARCHAR(500),
    website_url VARCHAR(500),
    country     VARCHAR(2), -- ISO 국가 코드
    status      VARCHAR(20) DEFAULT 'ACTIVE',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_brands_slug (slug),
    INDEX idx_brands_status (status)
);

-- 상품 테이블 (마이크로서비스 환경)
CREATE TABLE IF NOT EXISTS products
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku             VARCHAR(50) UNIQUE NOT NULL,
    name            VARCHAR(200)       NOT NULL,
    slug            VARCHAR(250) UNIQUE NOT NULL,
    short_description TEXT,
    description     LONGTEXT,
    category_id     BIGINT,
    brand_id        BIGINT,
    price           DECIMAL(12, 2)     NOT NULL,
    compare_price   DECIMAL(12, 2),    -- 비교가격
    cost_price      DECIMAL(12, 2),    -- 원가
    currency        VARCHAR(3) DEFAULT 'KRW',
    tax_rate        DECIMAL(5, 4) DEFAULT 0.10, -- 세율
    weight          DECIMAL(8, 3),
    dimensions      JSON,              -- 가로/세로/높이
    images          JSON,              -- 이미지 URL 배열
    tags            JSON,              -- 태그 배열
    attributes      JSON,              -- 제품 속성 (색상, 사이즈 등)
    variants        JSON,              -- 상품 변형 정보
    seo_title       VARCHAR(200),
    seo_description VARCHAR(500),
    seo_keywords    VARCHAR(200),
    inventory_tracking BOOLEAN DEFAULT TRUE,
    requires_shipping BOOLEAN DEFAULT TRUE,
    is_digital      BOOLEAN     DEFAULT FALSE,
    is_featured     BOOLEAN     DEFAULT FALSE,
    status          VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, DISCONTINUED
    published_at    DATETIME,
    created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version         BIGINT      DEFAULT 0,
    
    FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
    FOREIGN KEY (brand_id) REFERENCES brands (id) ON DELETE SET NULL,
    INDEX idx_products_category_id (category_id),
    INDEX idx_products_brand_id (brand_id),
    INDEX idx_products_sku (sku),
    INDEX idx_products_slug (slug),
    INDEX idx_products_status (status),
    INDEX idx_products_price (price),
    INDEX idx_products_is_featured (is_featured),
    FULLTEXT INDEX ft_products_search (name, short_description, description)
);

-- 재고 관리 테이블
CREATE TABLE IF NOT EXISTS inventory_items
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id        BIGINT         NOT NULL,
    variant_id        VARCHAR(50),   -- 상품 변형 ID
    warehouse_id      BIGINT,
    quantity_available INT    DEFAULT 0,
    quantity_reserved INT    DEFAULT 0,
    quantity_on_order INT    DEFAULT 0,
    reorder_point     INT    DEFAULT 10,  -- 재주문 시점
    reorder_quantity  INT    DEFAULT 50,  -- 재주문 수량
    cost_per_unit     DECIMAL(10, 2) DEFAULT 0,
    last_counted_at   DATETIME,
    created_at        DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    UNIQUE KEY uk_product_variant_warehouse (product_id, variant_id, warehouse_id),
    INDEX idx_inventory_product_id (product_id),
    INDEX idx_inventory_warehouse_id (warehouse_id),
    INDEX idx_inventory_quantity_available (quantity_available)
);

-- ==============================================
-- 주문 서비스 (Order Service)
-- ==============================================

-- 주문 테이블
CREATE TABLE IF NOT EXISTS orders
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number        VARCHAR(50) UNIQUE NOT NULL,
    user_id             BIGINT             NOT NULL,
    status              VARCHAR(20) DEFAULT 'PENDING', -- PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    payment_status      VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, FAILED, REFUNDED
    fulfillment_status  VARCHAR(20) DEFAULT 'UNFULFILLED', -- UNFULFILLED, PARTIAL, FULFILLED
    currency            VARCHAR(3)  DEFAULT 'KRW',
    subtotal            DECIMAL(12, 2)     NOT NULL,
    tax_amount          DECIMAL(12, 2) DEFAULT 0,
    shipping_amount     DECIMAL(12, 2) DEFAULT 0,
    discount_amount     DECIMAL(12, 2) DEFAULT 0,
    total_amount        DECIMAL(12, 2)     NOT NULL,
    shipping_address    JSON,              -- 배송 주소
    billing_address     JSON,              -- 청구 주소
    customer_info       JSON,              -- 고객 정보
    shipping_method     VARCHAR(50),
    payment_method      VARCHAR(50),
    notes               TEXT,
    metadata            JSON,              -- 확장 데이터
    cancelled_at        DATETIME,
    cancelled_reason    VARCHAR(200),
    processed_at        DATETIME,
    created_at          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version             BIGINT      DEFAULT 0,
    
    FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_payment_status (payment_status),
    INDEX idx_orders_order_number (order_number),
    INDEX idx_orders_created_at (created_at)
);

-- 주문 상세 테이블
CREATE TABLE IF NOT EXISTS order_items
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT         NOT NULL,
    product_id      BIGINT         NOT NULL,
    variant_id      VARCHAR(50),
    product_name    VARCHAR(200)   NOT NULL,
    product_sku     VARCHAR(50),
    quantity        INT            NOT NULL,
    unit_price      DECIMAL(10, 2) NOT NULL,
    total_price     DECIMAL(12, 2) NOT NULL,
    product_snapshot JSON,          -- 주문 시점의 상품 정보
    fulfillment_status VARCHAR(20) DEFAULT 'UNFULFILLED',
    created_at      DATETIME       DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_id (product_id)
);

-- 배송 테이블
CREATE TABLE IF NOT EXISTS shipments
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id            BIGINT      NOT NULL,
    tracking_number     VARCHAR(100) UNIQUE,
    carrier             VARCHAR(50),
    service_type        VARCHAR(50),
    status              VARCHAR(20) DEFAULT 'PREPARING', -- PREPARING, SHIPPED, IN_TRANSIT, DELIVERED, EXCEPTION
    shipped_at          DATETIME,
    estimated_delivery  DATETIME,
    delivered_at        DATETIME,
    shipping_address    JSON,
    weight              DECIMAL(8, 3),
    dimensions          JSON,
    shipping_cost       DECIMAL(10, 2) DEFAULT 0,
    insurance_cost      DECIMAL(10, 2) DEFAULT 0,
    tracking_events     JSON,       -- 배송 추적 이벤트
    notes               TEXT,
    created_at          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    INDEX idx_shipments_order_id (order_id),
    INDEX idx_shipments_tracking_number (tracking_number),
    INDEX idx_shipments_status (status)
);

-- ==============================================
-- 결제 서비스 (Payment Service)
-- ==============================================

-- 결제 테이블
CREATE TABLE IF NOT EXISTS payments
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id          VARCHAR(100) UNIQUE NOT NULL, -- 외부 결제 시스템 ID
    order_id            BIGINT             NOT NULL,
    user_id             BIGINT             NOT NULL,
    amount              DECIMAL(12, 2)     NOT NULL,
    currency            VARCHAR(3) DEFAULT 'KRW',
    method              VARCHAR(50)        NOT NULL, -- CARD, BANK_TRANSFER, PAYPAL, etc.
    provider            VARCHAR(50),       -- 결제 대행사
    status              VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED
    gateway_response    JSON,              -- 결제 게이트웨이 응답
    transaction_id      VARCHAR(100),      -- 거래 ID
    reference_id        VARCHAR(100),      -- 참조 ID
    failure_reason      VARCHAR(200),
    processed_at        DATETIME,
    refunded_at         DATETIME,
    refund_amount       DECIMAL(12, 2) DEFAULT 0,
    refund_reason       VARCHAR(200),
    metadata            JSON,
    created_at          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_user_id (user_id),
    INDEX idx_payments_payment_id (payment_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_processed_at (processed_at)
);

-- ==============================================
-- 알림 서비스 (Notification Service)
-- ==============================================

-- 알림 템플릿 테이블
CREATE TABLE IF NOT EXISTS notification_templates
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    type        VARCHAR(50)         NOT NULL, -- EMAIL, SMS, PUSH, WEBHOOK
    subject     VARCHAR(200),
    body        LONGTEXT            NOT NULL,
    variables   JSON,               -- 템플릿 변수
    is_active   BOOLEAN     DEFAULT TRUE,
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_notification_templates_type (type),
    INDEX idx_notification_templates_is_active (is_active)
);

-- 알림 발송 내역 테이블
CREATE TABLE IF NOT EXISTS notifications
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id  BIGINT,
    user_id      BIGINT,
    type         VARCHAR(50)        NOT NULL,
    recipient    VARCHAR(200)       NOT NULL, -- 이메일, 전화번호, 디바이스 토큰 등
    subject      VARCHAR(200),
    content      LONGTEXT           NOT NULL,
    status       VARCHAR(20) DEFAULT 'PENDING', -- PENDING, SENT, DELIVERED, FAILED, BOUNCED
    scheduled_at DATETIME,
    sent_at      DATETIME,
    delivered_at DATETIME,
    error_message TEXT,
    metadata     JSON,
    created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (template_id) REFERENCES notification_templates (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_scheduled_at (scheduled_at)
);

-- ==============================================
-- 서비스 메시 및 모니터링
-- ==============================================

-- 서비스 등록 테이블
CREATE TABLE IF NOT EXISTS service_registry
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    instance_id  VARCHAR(100) NOT NULL,
    host         VARCHAR(100) NOT NULL,
    port         INT          NOT NULL,
    version      VARCHAR(50),
    status       VARCHAR(20) DEFAULT 'UP', -- UP, DOWN, OUT_OF_SERVICE
    health_check_url VARCHAR(500),
    metadata     JSON,                      -- 서비스 메타데이터
    tags         JSON,                      -- 서비스 태그
    registered_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    last_heartbeat DATETIME   DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_service_instance (service_name, instance_id),
    INDEX idx_service_registry_service_name (service_name),
    INDEX idx_service_registry_status (status),
    INDEX idx_service_registry_last_heartbeat (last_heartbeat)
);

-- API 호출 로그 테이블
CREATE TABLE IF NOT EXISTS api_call_logs
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id       VARCHAR(100) NOT NULL,
    service_name     VARCHAR(100),
    method           VARCHAR(10)  NOT NULL, -- GET, POST, PUT, DELETE
    endpoint         VARCHAR(500) NOT NULL,
    user_id          BIGINT,
    api_key_id       VARCHAR(50),
    client_ip        VARCHAR(45),
    user_agent       VARCHAR(500),
    request_headers  JSON,
    request_body     LONGTEXT,
    response_status  INT,
    response_headers JSON,
    response_body    LONGTEXT,
    response_time_ms INT,
    error_message    TEXT,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_api_call_logs_request_id (request_id),
    INDEX idx_api_call_logs_service_name (service_name),
    INDEX idx_api_call_logs_user_id (user_id),
    INDEX idx_api_call_logs_created_at (created_at),
    INDEX idx_api_call_logs_response_status (response_status)
);

-- 분산 트레이싱 테이블
CREATE TABLE IF NOT EXISTS distributed_traces
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    trace_id     VARCHAR(100) NOT NULL,
    span_id      VARCHAR(100) NOT NULL,
    parent_span_id VARCHAR(100),
    service_name VARCHAR(100) NOT NULL,
    operation_name VARCHAR(200),
    start_time   DATETIME     NOT NULL,
    end_time     DATETIME     NOT NULL,
    duration_ms  INT          NOT NULL,
    status       VARCHAR(20),    -- OK, ERROR, TIMEOUT
    tags         JSON,
    logs         JSON,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_distributed_traces_trace_id (trace_id),
    INDEX idx_distributed_traces_span_id (span_id),
    INDEX idx_distributed_traces_service_name (service_name),
    INDEX idx_distributed_traces_start_time (start_time)
);

-- 서킷 브레이커 상태 테이블
CREATE TABLE IF NOT EXISTS circuit_breaker_states
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name    VARCHAR(100) NOT NULL,
    target_service  VARCHAR(100) NOT NULL,
    state           VARCHAR(20) DEFAULT 'CLOSED', -- CLOSED, OPEN, HALF_OPEN
    failure_count   INT         DEFAULT 0,
    success_count   INT         DEFAULT 0,
    last_failure_at DATETIME,
    last_success_at DATETIME,
    opened_at       DATETIME,
    next_attempt_at DATETIME,
    created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_circuit_breaker (service_name, target_service),
    INDEX idx_circuit_breaker_state (state),
    INDEX idx_circuit_breaker_next_attempt (next_attempt_at)
);

-- ==============================================
-- 초기 데이터 삽입
-- ==============================================

-- 사용자 데이터
INSERT IGNORE INTO users (email, password, nickname, first_name, last_name, phone, email_verified, status) VALUES 
('admin@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'SuperAdmin', '슈퍼', '관리자', '010-0000-0000', TRUE, 'ACTIVE'),
('manager@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ServiceManager', '서비스', '매니저', '010-1111-1111', TRUE, 'ACTIVE'),
('customer1@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Customer1', '고객', '일호', '010-2222-2222', TRUE, 'ACTIVE'),
('customer2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Customer2', '고객', '이호', '010-3333-3333', TRUE, 'ACTIVE');

-- 역할 데이터 (마이크로서비스별)
INSERT IGNORE INTO roles (name, display_name, description, service_name, permissions, is_system) VALUES 
('ADMIN', '시스템 관리자', '모든 서비스에 대한 관리 권한', 'ALL', '["*:*"]', TRUE),
('USER_SERVICE_ADMIN', '사용자 서비스 관리자', '사용자 서비스 관리 권한', 'USER_SERVICE', '["user:*", "role:*"]', TRUE),
('PRODUCT_SERVICE_ADMIN', '상품 서비스 관리자', '상품 서비스 관리 권한', 'PRODUCT_SERVICE', '["product:*", "category:*", "brand:*"]', TRUE),
('ORDER_SERVICE_ADMIN', '주문 서비스 관리자', '주문 서비스 관리 권한', 'ORDER_SERVICE', '["order:*", "shipment:*"]', TRUE),
('CUSTOMER', '일반 고객', '일반 고객 권한', 'CUSTOMER_SERVICE', '["product:read", "order:create", "order:read"]', FALSE);

-- 사용자 역할 매핑
INSERT IGNORE INTO user_roles (user_id, role_id, granted_by) VALUES 
(1, 1, NULL), -- 슈퍼 관리자
(2, 2, 1),    -- 사용자 서비스 관리자
(2, 3, 1),    -- 상품 서비스 관리자
(2, 4, 1),    -- 주문 서비스 관리자
(3, 5, NULL), -- 일반 고객
(4, 5, NULL); -- 일반 고객

-- API 키 데이터
INSERT IGNORE INTO api_keys (key_id, key_hash, service_name, description, permissions, rate_limit) VALUES 
('frontend-app-key', '$2a$10$FrontendAppKeyHashValue...', 'FRONTEND_APP', '프론트엔드 애플리케이션 API 키', '["product:read", "order:create", "user:profile"]', 5000),
('mobile-app-key', '$2a$10$MobileAppKeyHashValue...', 'MOBILE_APP', '모바일 애플리케이션 API 키', '["product:read", "order:create", "user:profile"]', 3000),
('partner-api-key', '$2a$10$PartnerApiKeyHashValue...', 'PARTNER_SYSTEM', '파트너 시스템 API 키', '["product:read", "order:read"]', 1000);

-- 카테고리 데이터
INSERT IGNORE INTO categories (name, slug, description, level, sort_order, is_featured) VALUES 
('전자기기', 'electronics', '전자제품 카테고리', 1, 1, TRUE),
('컴퓨터', 'computers', '컴퓨터 및 주변기기', 2, 1, TRUE),
('스마트폰', 'smartphones', '스마트폰 및 액세서리', 2, 2, TRUE),
('도서', 'books', '각종 도서', 1, 2, FALSE),
('기술서적', 'tech-books', '프로그래밍 및 기술 도서', 2, 1, TRUE);

-- 카테고리 계층 구조 설정
UPDATE categories SET parent_id = 1 WHERE slug IN ('computers', 'smartphones');
UPDATE categories SET parent_id = 4 WHERE slug = 'tech-books';

-- 브랜드 데이터
INSERT IGNORE INTO brands (name, slug, description, country) VALUES 
('Samsung', 'samsung', '삼성전자', 'KR'),
('Apple', 'apple', '애플', 'US'),
('LG', 'lg', 'LG전자', 'KR'),
('Packt', 'packt', 'Packt Publishing', 'UK'),
('OReilly', 'oreilly', 'O\'Reilly Media', 'US');

-- 상품 데이터
INSERT IGNORE INTO products (sku, name, slug, short_description, description, category_id, brand_id, price, compare_price, currency, images, tags, is_featured, status, published_at) VALUES 
('GALAXY-S24-256', 'Galaxy S24 256GB', 'galaxy-s24-256gb', '삼성 최신 플래그십 스마트폰', '<h2>Galaxy S24</h2><p>AI 기반 최신 기능이 탑재된 프리미엄 스마트폰입니다.</p>', 3, 1, 1200000.00, 1300000.00, 'KRW', '["https://images.example.com/galaxy-s24-1.jpg", "https://images.example.com/galaxy-s24-2.jpg"]', '["스마트폰", "5G", "AI카메라"]', TRUE, 'ACTIVE', NOW()),
('IPHONE-15-PRO', 'iPhone 15 Pro 256GB', 'iphone-15-pro-256gb', '애플 아이폰 15 프로', '<h2>iPhone 15 Pro</h2><p>티타늄 소재의 프리미엄 아이폰입니다.</p>', 3, 2, 1500000.00, 1600000.00, 'KRW', '["https://images.example.com/iphone-15-pro-1.jpg"]', '["아이폰", "티타늄", "프로카메라"]', TRUE, 'ACTIVE', NOW()),
('SPRING-BOOT-GUIDE', 'Spring Boot 완벽 가이드', 'spring-boot-complete-guide', 'Spring Boot 마스터하기', '<p>Spring Boot의 모든 것을 배울 수 있는 완벽한 가이드북입니다.</p>', 5, 4, 45000.00, 50000.00, 'KRW', '["https://images.example.com/spring-boot-book.jpg"]', '["Spring Boot", "Java", "백엔드"]', TRUE, 'ACTIVE', NOW()),
('MICROSERVICES-PATTERN', '마이크로서비스 패턴', 'microservices-patterns', '마이크로서비스 아키텍처 설계', '<p>마이크로서비스 아키텍처의 패턴과 실무 적용 방법을 다룹니다.</p>', 5, 5, 42000.00, NULL, 'KRW', '["https://images.example.com/microservices-book.jpg"]', '["마이크로서비스", "아키텍처", "분산시스템"]', FALSE, 'ACTIVE', NOW());

-- 재고 데이터
INSERT IGNORE INTO inventory_items (product_id, quantity_available, reorder_point, reorder_quantity, cost_per_unit) VALUES 
(1, 50, 10, 30, 1000000.00),
(2, 30, 5, 20, 1250000.00),
(3, 100, 20, 50, 30000.00),
(4, 80, 15, 40, 28000.00);

-- 주문 데이터
INSERT IGNORE INTO orders (order_number, user_id, status, payment_status, currency, subtotal, tax_amount, shipping_amount, total_amount, shipping_address, billing_address, shipping_method, payment_method) VALUES 
('ORD-2024-000001', 3, 'CONFIRMED', 'PAID', 'KRW', 1200000.00, 120000.00, 3000.00, 1323000.00, 
 '{"name": "고객 일호", "phone": "010-2222-2222", "address": "서울시 강남구 테헤란로 123", "zipCode": "12345"}',
 '{"name": "고객 일호", "phone": "010-2222-2222", "address": "서울시 강남구 테헤란로 123", "zipCode": "12345"}',
 'STANDARD', 'CARD'),
('ORD-2024-000002', 4, 'PROCESSING', 'PAID', 'KRW', 87000.00, 8700.00, 3000.00, 98700.00,
 '{"name": "고객 이호", "phone": "010-3333-3333", "address": "서울시 서초구 강남대로 456", "zipCode": "67890"}',
 '{"name": "고객 이호", "phone": "010-3333-3333", "address": "서울시 서초구 강남대로 456", "zipCode": "67890"}',
 'EXPRESS', 'CARD');

-- 주문 상세 데이터
INSERT IGNORE INTO order_items (order_id, product_id, product_name, product_sku, quantity, unit_price, total_price, product_snapshot) VALUES 
(1, 1, 'Galaxy S24 256GB', 'GALAXY-S24-256', 1, 1200000.00, 1200000.00, '{"color": "Phantom Black", "storage": "256GB"}'),
(2, 3, 'Spring Boot 완벽 가이드', 'SPRING-BOOT-GUIDE', 1, 45000.00, 45000.00, '{"edition": "2024", "pages": 800}'),
(2, 4, '마이크로서비스 패턴', 'MICROSERVICES-PATTERN', 1, 42000.00, 42000.00, '{"edition": "Korean", "translator": "김개발"}');

-- 결제 데이터
INSERT IGNORE INTO payments (payment_id, order_id, user_id, amount, currency, method, provider, status, transaction_id, processed_at) VALUES 
('PAY-2024-000001', 1, 3, 1323000.00, 'KRW', 'CARD', 'NICE_PAY', 'COMPLETED', 'TXN-ABC123456', NOW()),
('PAY-2024-000002', 2, 4, 98700.00, 'KRW', 'CARD', 'NICE_PAY', 'COMPLETED', 'TXN-DEF789012', NOW());

-- 배송 데이터
INSERT IGNORE INTO shipments (order_id, tracking_number, carrier, service_type, status, shipping_address, weight, shipping_cost) VALUES 
(1, 'TRK-2024-001', '대한통운', 'STANDARD', 'SHIPPED', 
 '{"name": "고객 일호", "phone": "010-2222-2222", "address": "서울시 강남구 테헤란로 123", "zipCode": "12345"}',
 0.250, 3000.00),
(2, 'TRK-2024-002', 'CJ택배', 'EXPRESS', 'IN_TRANSIT',
 '{"name": "고객 이호", "phone": "010-3333-3333", "address": "서울시 서초구 강남대로 456", "zipCode": "67890"}',
 0.800, 3000.00);

-- 알림 템플릿 데이터
INSERT IGNORE INTO notification_templates (name, type, subject, body, variables) VALUES 
('order_confirmed', 'EMAIL', '주문이 확인되었습니다', '<h1>주문 확인</h1><p>{{customerName}}님의 주문 {{orderNumber}}이 확인되었습니다.</p>', '["customerName", "orderNumber", "totalAmount"]'),
('order_shipped', 'EMAIL', '상품이 발송되었습니다', '<h1>발송 완료</h1><p>주문하신 상품이 발송되었습니다. 운송장번호: {{trackingNumber}}</p>', '["customerName", "orderNumber", "trackingNumber"]'),
('welcome_user', 'EMAIL', '가입을 환영합니다', '<h1>환영합니다!</h1><p>{{name}}님, 프리마베라에 가입해주셔서 감사합니다.</p>', '["name", "email"]');

-- 서비스 등록 데이터
INSERT IGNORE INTO service_registry (service_name, instance_id, host, port, version, status, health_check_url, metadata, tags) VALUES 
('user-service', 'user-service-1', 'user-service-pod-1', 8080, '1.0.0', 'UP', '/actuator/health', '{"zone": "zone-a"}', '["authentication", "user-management"]'),
('product-service', 'product-service-1', 'product-service-pod-1', 8081, '1.0.0', 'UP', '/actuator/health', '{"zone": "zone-a"}', '["catalog", "inventory"]'),
('order-service', 'order-service-1', 'order-service-pod-1', 8082, '1.0.0', 'UP', '/actuator/health', '{"zone": "zone-b"}', '["orders", "fulfillment"]'),
('payment-service', 'payment-service-1', 'payment-service-pod-1', 8083, '1.0.0', 'UP', '/actuator/health', '{"zone": "zone-a"}', '["payments", "billing"]'),
('notification-service', 'notification-service-1', 'notification-service-pod-1', 8084, '1.0.0', 'UP', '/actuator/health', '{"zone": "zone-c"}', '["notifications", "messaging"]');

-- 서킷 브레이커 상태 초기화
INSERT IGNORE INTO circuit_breaker_states (service_name, target_service, state, failure_count, success_count) VALUES 
('order-service', 'product-service', 'CLOSED', 0, 10),
('order-service', 'payment-service', 'CLOSED', 0, 15),
('order-service', 'notification-service', 'CLOSED', 0, 8),
('payment-service', 'user-service', 'CLOSED', 0, 12);

-- ==============================================
-- 종료 메시지
-- ==============================================

SELECT 'Primavera Microservices Environment Database Initialization Completed!' as STATUS;