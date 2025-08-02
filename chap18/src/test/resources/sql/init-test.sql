-- ==============================================
-- Chapter 18 - Microservices Architecture Test Data
-- Uses primavera_test database (TestContainers)
-- Focus: Microservices communication, service discovery, distributed systems
-- ==============================================

-- 사용자 테이블 (Account Service용)
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    FIRST_NAME VARCHAR(50),
    LAST_NAME  VARCHAR(50),
    STATUS     VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_USERS_EMAIL (EMAIL),
    INDEX IDX_USERS_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 상품 테이블 (Product Service용)
CREATE TABLE IF NOT EXISTS PRODUCTS
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME        VARCHAR(200)   NOT NULL,
    DESCRIPTION LONGTEXT,
    PRICE       DECIMAL(10, 2) NOT NULL,
    STOCK       INT            NOT NULL DEFAULT 0,
    CATEGORY    VARCHAR(100),
    STATUS      VARCHAR(20)    DEFAULT 'ACTIVE',
    CREATED_AT  DATETIME       DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT  DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_PRODUCTS_CATEGORY (CATEGORY),
    INDEX IDX_PRODUCTS_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 주문 테이블 (Order Service용)
CREATE TABLE IF NOT EXISTS ORDERS
(
    ID           BIGINT AUTO_INCREMENT PRIMARY KEY,
    ORDER_NUMBER VARCHAR(50) UNIQUE NOT NULL,
    USER_ID      BIGINT             NOT NULL,
    TOTAL_AMOUNT DECIMAL(12, 2)     NOT NULL,
    STATUS       VARCHAR(20)        DEFAULT 'PENDING',
    ORDER_DATE   DATETIME           DEFAULT CURRENT_TIMESTAMP,
    SHIPPING_ADDRESS TEXT,
    PAYMENT_METHOD VARCHAR(50),
    CREATED_AT   DATETIME           DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT   DATETIME           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_ORDERS_USER_ID (USER_ID),
    INDEX IDX_ORDERS_STATUS (STATUS),
    INDEX IDX_ORDERS_ORDER_DATE (ORDER_DATE)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 주문 항목 테이블
CREATE TABLE IF NOT EXISTS ORDER_ITEMS
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    ORDER_ID    BIGINT         NOT NULL,
    PRODUCT_ID  BIGINT         NOT NULL,
    QUANTITY    INT            NOT NULL,
    UNIT_PRICE  DECIMAL(10, 2) NOT NULL,
    TOTAL_PRICE DECIMAL(12, 2) NOT NULL,
    
    FOREIGN KEY (ORDER_ID) REFERENCES ORDERS (ID) ON DELETE CASCADE,
    INDEX IDX_ORDER_ITEMS_ORDER_ID (ORDER_ID),
    INDEX IDX_ORDER_ITEMS_PRODUCT_ID (PRODUCT_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 서비스 레지스트리 테이블 (Service Discovery용)
CREATE TABLE IF NOT EXISTS SERVICE_REGISTRY
(
    ID              BIGINT AUTO_INCREMENT PRIMARY KEY,
    SERVICE_NAME    VARCHAR(100) NOT NULL,
    SERVICE_ID      VARCHAR(100) UNIQUE NOT NULL,
    HOST            VARCHAR(255) NOT NULL,
    PORT            INT          NOT NULL,
    PROTOCOL        VARCHAR(10)  DEFAULT 'http',
    HEALTH_CHECK_URL VARCHAR(500),
    STATUS          VARCHAR(20)  DEFAULT 'UP',
    METADATA        JSON,                    -- 추가 서비스 메타데이터
    REGISTERED_AT   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    LAST_HEARTBEAT  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    
    INDEX IDX_SERVICE_REGISTRY_NAME (SERVICE_NAME),
    INDEX IDX_SERVICE_REGISTRY_STATUS (STATUS),
    INDEX IDX_SERVICE_REGISTRY_HEARTBEAT (LAST_HEARTBEAT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- API 게이트웨이 라우팅 테이블
CREATE TABLE IF NOT EXISTS API_ROUTES
(
    ID            BIGINT AUTO_INCREMENT PRIMARY KEY,
    ROUTE_ID      VARCHAR(100) UNIQUE NOT NULL,
    PATH_PATTERN  VARCHAR(255)        NOT NULL,
    SERVICE_NAME  VARCHAR(100)        NOT NULL,
    METHOD        VARCHAR(10)         DEFAULT 'GET',
    LOAD_BALANCER VARCHAR(50)         DEFAULT 'ROUND_ROBIN',
    TIMEOUT_MS    INT                 DEFAULT 30000,
    RETRY_COUNT   INT                 DEFAULT 3,
    RATE_LIMIT    INT                 DEFAULT 1000, -- per minute
    AUTH_REQUIRED BOOLEAN             DEFAULT TRUE,
    ACTIVE        BOOLEAN             DEFAULT TRUE,
    CREATED_AT    DATETIME            DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT    DATETIME            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_API_ROUTES_PATH (PATH_PATTERN),
    INDEX IDX_API_ROUTES_SERVICE (SERVICE_NAME),
    INDEX IDX_API_ROUTES_ACTIVE (ACTIVE)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 분산 트랜잭션 로그 테이블 (Saga Pattern용)
CREATE TABLE IF NOT EXISTS SAGA_TRANSACTIONS
(
    ID                BIGINT AUTO_INCREMENT PRIMARY KEY,
    SAGA_ID           VARCHAR(100) UNIQUE NOT NULL,
    TRANSACTION_TYPE  VARCHAR(100)        NOT NULL,
    STATUS            VARCHAR(20)         DEFAULT 'STARTED',
    CURRENT_STEP      INT                 DEFAULT 0,
    TOTAL_STEPS       INT                 NOT NULL,
    PAYLOAD           JSON,                        -- 트랜잭션 데이터
    COMPENSATIONS     JSON,                        -- 보상 액션 목록
    ERROR_MESSAGE     TEXT,
    STARTED_AT        DATETIME            DEFAULT CURRENT_TIMESTAMP,
    COMPLETED_AT      DATETIME,
    LAST_UPDATED      DATETIME            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_SAGA_ID (SAGA_ID),
    INDEX IDX_SAGA_STATUS (STATUS),
    INDEX IDX_SAGA_TYPE (TRANSACTION_TYPE)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 이벤트 저장소 테이블 (Event Sourcing용)
CREATE TABLE IF NOT EXISTS EVENT_STORE
(
    ID               BIGINT AUTO_INCREMENT PRIMARY KEY,
    AGGREGATE_ID     VARCHAR(100) NOT NULL,
    AGGREGATE_TYPE   VARCHAR(100) NOT NULL,
    EVENT_TYPE       VARCHAR(100) NOT NULL,
    EVENT_VERSION    INT          NOT NULL,
    EVENT_DATA       JSON         NOT NULL,
    METADATA         JSON,
    OCCURRED_AT      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PROCESSED        BOOLEAN      DEFAULT FALSE,
    PROCESSING_ERROR TEXT,
    
    UNIQUE KEY UK_AGGREGATE_VERSION (AGGREGATE_ID, EVENT_VERSION),
    INDEX IDX_EVENT_AGGREGATE (AGGREGATE_ID, AGGREGATE_TYPE),
    INDEX IDX_EVENT_TYPE (EVENT_TYPE),
    INDEX IDX_EVENT_OCCURRED (OCCURRED_AT),
    INDEX IDX_EVENT_PROCESSED (PROCESSED)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 서비스 간 통신 로그 테이블
CREATE TABLE IF NOT EXISTS SERVICE_COMMUNICATION_LOGS
(
    ID               BIGINT AUTO_INCREMENT PRIMARY KEY,
    CORRELATION_ID   VARCHAR(100),
    SOURCE_SERVICE   VARCHAR(100) NOT NULL,
    TARGET_SERVICE   VARCHAR(100) NOT NULL,
    OPERATION        VARCHAR(100) NOT NULL,
    REQUEST_METHOD   VARCHAR(10),
    REQUEST_URL      VARCHAR(500),
    REQUEST_PAYLOAD  JSON,
    RESPONSE_STATUS  INT,
    RESPONSE_PAYLOAD JSON,
    DURATION_MS      BIGINT,
    SUCCESS          BOOLEAN,
    ERROR_MESSAGE    TEXT,
    TIMESTAMP        DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX IDX_COMM_CORRELATION (CORRELATION_ID),
    INDEX IDX_COMM_SOURCE (SOURCE_SERVICE),
    INDEX IDX_COMM_TARGET (TARGET_SERVICE),
    INDEX IDX_COMM_TIMESTAMP (TIMESTAMP)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터 - Chapter 18: Microservices Architecture
INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, FIRST_NAME, LAST_NAME, STATUS) VALUES 
('microservice@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'MicroAdmin', 'Micro', 'Admin', 'ACTIVE'),
('customer@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Customer', 'Test', 'Customer', 'ACTIVE'),
('merchant@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Merchant', 'Product', 'Merchant', 'ACTIVE')
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO PRODUCTS (NAME, DESCRIPTION, PRICE, STOCK, CATEGORY, STATUS) VALUES 
('Microservices Book', 'Building Microservices: Designing Fine-Grained Systems', 59.99, 100, 'Books', 'ACTIVE'),
('Spring Cloud Kit', 'Complete Spring Cloud development toolkit', 199.99, 50, 'Software', 'ACTIVE'),
('Docker Container', 'Docker Professional Container Platform', 299.99, 25, 'DevOps', 'ACTIVE'),
('Kubernetes License', 'Kubernetes Orchestration Platform License', 999.99, 10, 'DevOps', 'ACTIVE')
ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);

INSERT INTO ORDERS (ORDER_NUMBER, USER_ID, TOTAL_AMOUNT, STATUS, SHIPPING_ADDRESS, PAYMENT_METHOD) VALUES 
('MSA-2024-001', 2, 59.99, 'COMPLETED', '123 Microservice St, Cloud City', 'CREDIT_CARD'),
('MSA-2024-002', 2, 499.98, 'PROCESSING', '123 Microservice St, Cloud City', 'PAYPAL'),
('MSA-2024-003', 3, 1299.98, 'PENDING', '456 Container Ave, Docker Town', 'BANK_TRANSFER')
ON DUPLICATE KEY UPDATE ORDER_NUMBER = VALUES(ORDER_NUMBER);

INSERT INTO ORDER_ITEMS (ORDER_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE, TOTAL_PRICE) VALUES 
(1, 1, 1, 59.99, 59.99),
(2, 1, 1, 59.99, 59.99),
(2, 2, 1, 199.99, 199.99),
(2, 3, 1, 299.99, 299.99),
(3, 4, 1, 999.99, 999.99),
(3, 3, 1, 299.99, 299.99)
ON DUPLICATE KEY UPDATE ORDER_ID = VALUES(ORDER_ID);

INSERT INTO SERVICE_REGISTRY (SERVICE_NAME, SERVICE_ID, HOST, PORT, PROTOCOL, HEALTH_CHECK_URL, STATUS, METADATA) VALUES 
('account-service', 'account-001', 'localhost', 8081, 'http', '/actuator/health', 'UP', '{"version": "1.0.0", "zone": "us-east-1"}'),
('product-service', 'product-001', 'localhost', 8082, 'http', '/actuator/health', 'UP', '{"version": "1.0.0", "zone": "us-east-1"}'),
('order-service', 'order-001', 'localhost', 8083, 'http', '/actuator/health', 'UP', '{"version": "1.0.0", "zone": "us-east-1"}'),
('front-service', 'front-001', 'localhost', 8080, 'http', '/actuator/health', 'UP', '{"version": "1.0.0", "zone": "us-east-1", "type": "gateway"}')
ON DUPLICATE KEY UPDATE SERVICE_ID = VALUES(SERVICE_ID);

INSERT INTO API_ROUTES (ROUTE_ID, PATH_PATTERN, SERVICE_NAME, METHOD, LOAD_BALANCER, AUTH_REQUIRED) VALUES 
('users-route', '/api/users/**', 'account-service', 'GET', 'ROUND_ROBIN', TRUE),
('products-route', '/api/products/**', 'product-service', 'GET', 'ROUND_ROBIN', FALSE),
('orders-route', '/api/orders/**', 'order-service', 'POST', 'ROUND_ROBIN', TRUE),
('health-route', '/actuator/**', 'ALL', 'GET', 'ROUND_ROBIN', FALSE)
ON DUPLICATE KEY UPDATE ROUTE_ID = VALUES(ROUTE_ID);

INSERT INTO SAGA_TRANSACTIONS (SAGA_ID, TRANSACTION_TYPE, STATUS, CURRENT_STEP, TOTAL_STEPS, PAYLOAD) VALUES 
('saga-order-001', 'CREATE_ORDER', 'COMPLETED', 3, 3, '{"orderId": 1, "userId": 2, "amount": 59.99}'),
('saga-order-002', 'CREATE_ORDER', 'IN_PROGRESS', 2, 3, '{"orderId": 2, "userId": 2, "amount": 499.98}'),
('saga-order-003', 'CREATE_ORDER', 'FAILED', 1, 3, '{"orderId": 3, "userId": 3, "amount": 1299.98, "error": "Insufficient stock"}')
ON DUPLICATE KEY UPDATE SAGA_ID = VALUES(SAGA_ID);

INSERT INTO EVENT_STORE (AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, EVENT_VERSION, EVENT_DATA, METADATA) VALUES 
('user-2', 'User', 'UserRegistered', 1, '{"email": "customer@primavera.com", "nickname": "Customer"}', '{"service": "account-service"}'),
('order-1', 'Order', 'OrderCreated', 1, '{"orderNumber": "MSA-2024-001", "userId": 2, "amount": 59.99}', '{"service": "order-service"}'),
('order-1', 'Order', 'OrderCompleted', 2, '{"orderNumber": "MSA-2024-001", "completedAt": "2024-01-22T10:00:00"}', '{"service": "order-service"}'),
('product-1', 'Product', 'StockReduced', 1, '{"productId": 1, "quantity": 1, "remainingStock": 99}', '{"service": "product-service"}')
ON DUPLICATE KEY UPDATE AGGREGATE_ID = VALUES(AGGREGATE_ID);

INSERT INTO SERVICE_COMMUNICATION_LOGS (CORRELATION_ID, SOURCE_SERVICE, TARGET_SERVICE, OPERATION, REQUEST_METHOD, REQUEST_URL, RESPONSE_STATUS, DURATION_MS, SUCCESS) VALUES 
('corr-001', 'front-service', 'account-service', 'getUserById', 'GET', '/api/users/2', 200, 85, TRUE),
('corr-002', 'order-service', 'product-service', 'checkStock', 'GET', '/api/products/1/stock', 200, 45, TRUE),
('corr-003', 'order-service', 'account-service', 'validateUser', 'GET', '/api/users/2/validate', 200, 32, TRUE),
('corr-004', 'front-service', 'order-service', 'createOrder', 'POST', '/api/orders', 201, 150, TRUE)
ON DUPLICATE KEY UPDATE CORRELATION_ID = VALUES(CORRELATION_ID);