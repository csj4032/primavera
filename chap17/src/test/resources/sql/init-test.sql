-- ==============================================
-- Chapter 17 - Monitoring and Observability Test Data
-- Uses primavera_test database (TestContainers)
-- Focus: Application metrics, health checks, monitoring, alerting
-- ==============================================

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255)        NOT NULL,
    NICKNAME   VARCHAR(50)         NOT NULL,
    STATUS     VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_USERS_STATUS (STATUS)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 애플리케이션 메트릭 테이블
CREATE TABLE IF NOT EXISTS APPLICATION_METRICS
(
    ID          BIGINT AUTO_INCREMENT PRIMARY KEY,
    METRIC_NAME VARCHAR(100) NOT NULL,
    METRIC_TYPE VARCHAR(50)  NOT NULL, -- COUNTER, GAUGE, HISTOGRAM, TIMER
    VALUE       DOUBLE       NOT NULL,
    UNIT        VARCHAR(20),
    TAGS        JSON,                   -- 메트릭 태그 (key-value pairs)
    TIMESTAMP   DATETIME     NOT NULL,
    
    INDEX IDX_METRICS_NAME (METRIC_NAME),
    INDEX IDX_METRICS_TYPE (METRIC_TYPE),
    INDEX IDX_METRICS_TIMESTAMP (TIMESTAMP)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 시스템 헬스 체크 테이블
CREATE TABLE IF NOT EXISTS HEALTH_CHECKS
(
    ID              BIGINT AUTO_INCREMENT PRIMARY KEY,
    SERVICE_NAME    VARCHAR(100) NOT NULL,
    COMPONENT       VARCHAR(100) NOT NULL, -- database, redis, external-api, etc.
    STATUS          VARCHAR(20)  NOT NULL, -- UP, DOWN, OUT_OF_SERVICE, UNKNOWN
    DETAILS         JSON,                   -- 상세 정보
    RESPONSE_TIME_MS BIGINT,                -- 응답 시간 (밀리초)
    CHECK_TIME      DATETIME     NOT NULL,
    
    INDEX IDX_HEALTH_SERVICE (SERVICE_NAME),
    INDEX IDX_HEALTH_STATUS (STATUS),
    INDEX IDX_HEALTH_TIME (CHECK_TIME)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 성능 트레이스 테이블
CREATE TABLE IF NOT EXISTS PERFORMANCE_TRACES
(
    ID              BIGINT AUTO_INCREMENT PRIMARY KEY,
    TRACE_ID        VARCHAR(100) NOT NULL,
    SPAN_ID         VARCHAR(100) NOT NULL,
    PARENT_SPAN_ID  VARCHAR(100),
    OPERATION_NAME  VARCHAR(200) NOT NULL,
    SERVICE_NAME    VARCHAR(100) NOT NULL,
    START_TIME      DATETIME     NOT NULL,
    END_TIME        DATETIME     NOT NULL,
    DURATION_MS     BIGINT       NOT NULL,
    STATUS          VARCHAR(20)  DEFAULT 'OK', -- OK, ERROR, TIMEOUT
    TAGS            JSON,                       -- 추가 태그 정보
    LOGS            JSON,                       -- 로그 이벤트
    
    INDEX IDX_TRACES_TRACE_ID (TRACE_ID),
    INDEX IDX_TRACES_SPAN_ID (SPAN_ID),
    INDEX IDX_TRACES_SERVICE (SERVICE_NAME),
    INDEX IDX_TRACES_START_TIME (START_TIME)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 알람 규칙 테이블
CREATE TABLE IF NOT EXISTS ALERT_RULES
(
    ID              BIGINT AUTO_INCREMENT PRIMARY KEY,
    RULE_NAME       VARCHAR(100) NOT NULL,
    METRIC_NAME     VARCHAR(100) NOT NULL,
    CONDITION_TYPE  VARCHAR(20)  NOT NULL, -- GREATER_THAN, LESS_THAN, EQUALS, NOT_EQUALS
    THRESHOLD_VALUE DOUBLE       NOT NULL,
    DURATION_SECONDS INT         DEFAULT 300, -- 조건 지속 시간
    SEVERITY        VARCHAR(20)  DEFAULT 'WARNING', -- INFO, WARNING, CRITICAL
    ENABLED         BOOLEAN      DEFAULT TRUE,
    NOTIFICATION_CHANNELS JSON,                    -- 알림 채널 설정
    CREATED_AT      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_ALERT_RULES_METRIC (METRIC_NAME),
    INDEX IDX_ALERT_RULES_ENABLED (ENABLED)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 알람 이벤트 테이블
CREATE TABLE IF NOT EXISTS ALERT_EVENTS
(
    ID                BIGINT AUTO_INCREMENT PRIMARY KEY,
    RULE_ID           BIGINT       NOT NULL,
    ALERT_STATE       VARCHAR(20)  NOT NULL, -- FIRING, RESOLVED
    CURRENT_VALUE     DOUBLE       NOT NULL,
    THRESHOLD_VALUE   DOUBLE       NOT NULL,
    MESSAGE           TEXT,
    FIRED_AT          DATETIME     NOT NULL,
    RESOLVED_AT       DATETIME,
    DURATION_SECONDS  INT,
    ACKNOWLEDGED      BOOLEAN      DEFAULT FALSE,
    ACKNOWLEDGED_BY   BIGINT,
    ACKNOWLEDGED_AT   DATETIME,
    
    FOREIGN KEY (RULE_ID) REFERENCES ALERT_RULES (ID) ON DELETE CASCADE,
    FOREIGN KEY (ACKNOWLEDGED_BY) REFERENCES USERS (ID),
    INDEX IDX_ALERT_EVENTS_RULE_ID (RULE_ID),
    INDEX IDX_ALERT_EVENTS_STATE (ALERT_STATE),
    INDEX IDX_ALERT_EVENTS_FIRED_AT (FIRED_AT)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 서비스 종속성 테이블
CREATE TABLE IF NOT EXISTS SERVICE_DEPENDENCIES
(
    ID                BIGINT AUTO_INCREMENT PRIMARY KEY,
    SERVICE_NAME      VARCHAR(100) NOT NULL,
    DEPENDENCY_NAME   VARCHAR(100) NOT NULL,
    DEPENDENCY_TYPE   VARCHAR(50)  NOT NULL, -- DATABASE, CACHE, API, QUEUE
    ENDPOINT_URL      VARCHAR(500),
    TIMEOUT_MS        INT          DEFAULT 5000,
    RETRY_COUNT       INT          DEFAULT 3,
    CIRCUIT_BREAKER   BOOLEAN      DEFAULT TRUE,
    CRITICAL          BOOLEAN      DEFAULT TRUE, -- 중요한 종속성 여부
    CREATED_AT        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY UK_SERVICE_DEPENDENCY (SERVICE_NAME, DEPENDENCY_NAME),
    INDEX IDX_DEPENDENCIES_SERVICE (SERVICE_NAME),
    INDEX IDX_DEPENDENCIES_TYPE (DEPENDENCY_TYPE)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터 - Chapter 17: Monitoring and Observability
INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES 
('monitor@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'MonitorAdmin', 'ACTIVE'),
('ops@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'OpsEngineer', 'ACTIVE'),
('devops@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'DevOpsTeam', 'ACTIVE')
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

INSERT INTO APPLICATION_METRICS (METRIC_NAME, METRIC_TYPE, VALUE, UNIT, TAGS, TIMESTAMP) VALUES 
('http_requests_total', 'COUNTER', 15420, 'requests', '{"method": "GET", "status": "200", "endpoint": "/api/users"}', '2024-01-22 10:00:00'),
('http_request_duration', 'HISTOGRAM', 0.125, 'seconds', '{"method": "POST", "endpoint": "/api/orders"}', '2024-01-22 10:01:00'),
('jvm_memory_used', 'GAUGE', 768.5, 'MB', '{"area": "heap", "id": "PS Eden Space"}', '2024-01-22 10:02:00'),
('database_connections_active', 'GAUGE', 8, 'connections', '{"pool": "primary", "database": "primavera"}', '2024-01-22 10:03:00'),
('cache_hit_ratio', 'GAUGE', 0.87, 'ratio', '{"cache": "redis", "region": "user-sessions"}', '2024-01-22 10:04:00')
ON DUPLICATE KEY UPDATE METRIC_NAME = VALUES(METRIC_NAME);

INSERT INTO HEALTH_CHECKS (SERVICE_NAME, COMPONENT, STATUS, DETAILS, RESPONSE_TIME_MS, CHECK_TIME) VALUES 
('primavera-api', 'database', 'UP', '{"driver": "MariaDB", "version": "11.4.7", "active_connections": 8}', 45, '2024-01-22 10:05:00'),
('primavera-api', 'redis', 'UP', '{"version": "7.0", "used_memory": "2.1MB", "connected_clients": 5}', 12, '2024-01-22 10:05:00'),
('primavera-api', 'external-api', 'DOWN', '{"error": "Connection timeout", "endpoint": "https://api.external.com/v1/health"}', 5000, '2024-01-22 10:05:00'),
('primavera-batch', 'filesystem', 'UP', '{"disk_space": "85%", "temp_dir": "/tmp", "uploads_dir": "/uploads"}', 8, '2024-01-22 10:05:00')
ON DUPLICATE KEY UPDATE SERVICE_NAME = VALUES(SERVICE_NAME);

INSERT INTO PERFORMANCE_TRACES (TRACE_ID, SPAN_ID, PARENT_SPAN_ID, OPERATION_NAME, SERVICE_NAME, START_TIME, END_TIME, DURATION_MS, STATUS, TAGS) VALUES 
('trace-001', 'span-001', NULL, 'GET /api/users', 'primavera-api', '2024-01-22 10:10:00.000', '2024-01-22 10:10:00.150', 150, 'OK', '{"http.method": "GET", "http.url": "/api/users", "user.id": "123"}'),
('trace-001', 'span-002', 'span-001', 'UserService.findAllUsers', 'primavera-api', '2024-01-22 10:10:00.020', '2024-01-22 10:10:00.120', 100, 'OK', '{"component": "service", "db.statement": "SELECT * FROM users"}'),
('trace-002', 'span-003', NULL, 'POST /api/orders', 'primavera-api', '2024-01-22 10:15:00.000', '2024-01-22 10:15:00.500', 500, 'ERROR', '{"http.method": "POST", "error": "Validation failed"}')
ON DUPLICATE KEY UPDATE TRACE_ID = VALUES(TRACE_ID);

INSERT INTO ALERT_RULES (RULE_NAME, METRIC_NAME, CONDITION_TYPE, THRESHOLD_VALUE, DURATION_SECONDS, SEVERITY, NOTIFICATION_CHANNELS) VALUES 
('High Memory Usage', 'jvm_memory_used', 'GREATER_THAN', 900.0, 300, 'WARNING', '["email", "slack"]'),
('Low Cache Hit Ratio', 'cache_hit_ratio', 'LESS_THAN', 0.8, 600, 'WARNING', '["slack"]'),
('Database Connection Pool Exhausted', 'database_connections_active', 'GREATER_THAN', 18.0, 60, 'CRITICAL', '["email", "slack", "pagerduty"]'),
('High Error Rate', 'http_requests_error_rate', 'GREATER_THAN', 0.05, 180, 'CRITICAL', '["email", "slack", "pagerduty"]')
ON DUPLICATE KEY UPDATE RULE_NAME = VALUES(RULE_NAME);

INSERT INTO ALERT_EVENTS (RULE_ID, ALERT_STATE, CURRENT_VALUE, THRESHOLD_VALUE, MESSAGE, FIRED_AT, RESOLVED_AT, ACKNOWLEDGED, ACKNOWLEDGED_BY) VALUES 
(1, 'RESOLVED', 850.0, 900.0, 'Memory usage returned to normal levels', '2024-01-22 08:30:00', '2024-01-22 08:45:00', TRUE, 1),
(2, 'FIRING', 0.75, 0.8, 'Cache hit ratio below threshold for 10 minutes', '2024-01-22 10:00:00', NULL, FALSE, NULL),
(4, 'RESOLVED', 0.02, 0.05, 'Error rate spike resolved after service restart', '2024-01-22 09:15:00', '2024-01-22 09:30:00', TRUE, 2)
ON DUPLICATE KEY UPDATE RULE_ID = VALUES(RULE_ID);

INSERT INTO SERVICE_DEPENDENCIES (SERVICE_NAME, DEPENDENCY_NAME, DEPENDENCY_TYPE, ENDPOINT_URL, TIMEOUT_MS, CRITICAL) VALUES 
('primavera-api', 'mariadb-primary', 'DATABASE', 'jdbc:mariadb://localhost:3306/primavera', 5000, TRUE),
('primavera-api', 'redis-cache', 'CACHE', 'redis://localhost:6379', 2000, FALSE),
('primavera-api', 'external-payment-api', 'API', 'https://api.payment.com/v1', 10000, TRUE),
('primavera-batch', 'message-queue', 'QUEUE', 'amqp://localhost:5672', 3000, TRUE),
('primavera-frontend', 'primavera-api', 'API', 'http://localhost:8080/api', 5000, TRUE)
ON DUPLICATE KEY UPDATE SERVICE_NAME = VALUES(SERVICE_NAME);