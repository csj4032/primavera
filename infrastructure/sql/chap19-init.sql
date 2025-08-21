-- ==============================================
-- Database: primavera_performance (Chapter 19)
-- Performance Optimization with Virtual Threads and Caching
-- ==============================================

-- 사용자 테이블 (성능 최적화 적용)
CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(100) UNIQUE NOT NULL,
    name       VARCHAR(100)        NOT NULL,
    password   VARCHAR(255)        NOT NULL,
    active     BOOLEAN   DEFAULT TRUE,
    created_at DATETIME  DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 성능 최적화를 위한 인덱스
    INDEX idx_user_email (email),
    INDEX idx_user_active (active),
    INDEX idx_user_created (created_at),
    INDEX idx_user_name (name),
    INDEX idx_user_active_created (active, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 역할 테이블
CREATE TABLE IF NOT EXISTS roles
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_role_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자-역할 연결 테이블 (다대다 관계 최적화)
CREATE TABLE IF NOT EXISTS user_roles
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    
    -- 복합 인덱스로 성능 최적화
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_roles_user (user_id),
    INDEX idx_user_roles_role (role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 게시글 테이블 (N+1 문제 해결을 위한 구조)
CREATE TABLE IF NOT EXISTS posts
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    content    TEXT,
    user_id    BIGINT       NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    
    -- 성능 최적화 인덱스
    INDEX idx_post_user (user_id),
    INDEX idx_post_created (created_at),
    INDEX idx_post_user_created (user_id, created_at),
    INDEX idx_post_title (title)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 댓글 테이블
CREATE TABLE IF NOT EXISTS comments
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    content    TEXT   NOT NULL,
    post_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    
    -- 성능 인덱스
    INDEX idx_comment_post (post_id),
    INDEX idx_comment_user (user_id),
    INDEX idx_comment_created (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 성능 메트릭 테이블
CREATE TABLE IF NOT EXISTS performance_metrics
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metric_type  ENUM('JVM_MEMORY', 'JVM_THREAD', 'JVM_GC', 'DATABASE', 'CACHE', 'HTTP_REQUEST', 'VIRTUAL_THREAD', 'CUSTOM') NOT NULL,
    metric_name  VARCHAR(100) NOT NULL,
    metric_value DOUBLE       NOT NULL,
    unit         VARCHAR(20),
    tags         TEXT,
    
    -- 시계열 데이터 최적화 인덱스
    INDEX idx_metrics_timestamp (timestamp),
    INDEX idx_metrics_type (metric_type),
    INDEX idx_metrics_name (metric_name),
    INDEX idx_metrics_type_name_time (metric_type, metric_name, timestamp)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ==============================================
-- 기본 데이터 삽입
-- ==============================================

-- 기본 역할 데이터
INSERT INTO roles (id, name, description)
VALUES (1, 'ROLE_ADMINISTRATOR', '시스템 최고 관리자'),
       (2, 'ROLE_MANAGER', '시스템 관리자'),
       (3, 'ROLE_USER', '일반 사용자'),
       (4, 'ROLE_PERFORMANCE_TESTER', '성능 테스트 전용 역할')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 성능 테스트용 사용자 데이터 (대량 삽입 테스트용)
INSERT INTO users (id, email, name, password, active)
VALUES (1, 'admin@primavera.com', 'System Administrator', '{noop}admin123', TRUE),
       (2, 'perf.tester@primavera.com', 'Performance Tester', '{noop}test123', TRUE),
       (3, 'cache.user@primavera.com', 'Cache Test User', '{noop}cache123', TRUE),
       (4, 'virtual.thread@primavera.com', 'Virtual Thread User', '{noop}virtual123', TRUE),
       (5, 'query.optimizer@primavera.com', 'Query Optimizer', '{noop}query123', TRUE)
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- 사용자-역할 매핑
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1), (1, 2), (1, 3),  -- admin: 모든 역할
       (2, 4), (2, 3),          -- perf.tester: 성능 테스터 + 일반 사용자
       (3, 3),                  -- cache.user: 일반 사용자
       (4, 3),                  -- virtual.thread: 일반 사용자
       (5, 2), (5, 3)           -- query.optimizer: 관리자 + 일반 사용자
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- 성능 테스트용 게시글 데이터
INSERT INTO posts (title, content, user_id)
VALUES ('Virtual Threads Performance Test', 'Testing Virtual Threads with high concurrency', 2),
       ('Caching Strategy Analysis', 'Multi-layer caching performance evaluation', 3),
       ('JVM Tuning Results', 'G1GC optimization results and analysis', 5),
       ('Database Query Optimization', 'N+1 problem solving techniques', 5),
       ('Memory Usage Monitoring', 'Real-time JVM memory monitoring setup', 1);

-- 성능 테스트용 댓글 데이터
INSERT INTO comments (content, post_id, user_id)
VALUES ('Great analysis on Virtual Threads!', 1, 1),
       ('Cache hit rate improved significantly', 2, 2),
       ('GC pause time reduced by 80%', 3, 4),
       ('Batch processing shows 100x improvement', 4, 3),
       ('Memory leaks detected and fixed', 5, 2);

-- ==============================================
-- 성능 최적화를 위한 프로시저
-- ==============================================

DELIMITER //

-- 대량 사용자 생성 프로시저 (배치 테스트용)
CREATE PROCEDURE IF NOT EXISTS GenerateTestUsers(IN user_count INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 1000;
    
    WHILE i <= user_count DO
        INSERT INTO users (email, name, password, active)
        SELECT 
            CONCAT('testuser', i + seq, '@performance.test'),
            CONCAT('Test User ', i + seq),
            '{noop}password123',
            TRUE
        FROM (
            SELECT 0 AS seq UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
            SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) AS sequences
        WHERE i + seq <= user_count
        LIMIT batch_size;
        
        SET i = i + batch_size;
    END WHILE;
END //

-- 성능 메트릭 정리 프로시저 (오래된 데이터 삭제)
CREATE PROCEDURE IF NOT EXISTS CleanupOldMetrics(IN days_to_keep INT)
BEGIN
    DELETE FROM performance_metrics 
    WHERE timestamp < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    SELECT ROW_COUNT() AS deleted_rows;
END //

DELIMITER ;

-- ==============================================
-- 성능 모니터링을 위한 뷰
-- ==============================================

-- 사용자 통계 뷰
CREATE OR REPLACE VIEW user_statistics AS
SELECT 
    COUNT(*) AS total_users,
    COUNT(CASE WHEN active = TRUE THEN 1 END) AS active_users,
    COUNT(CASE WHEN active = FALSE THEN 1 END) AS inactive_users,
    COUNT(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) AS recent_users
FROM users;

-- 성능 메트릭 요약 뷰
CREATE OR REPLACE VIEW performance_summary AS
SELECT 
    metric_type,
    metric_name,
    COUNT(*) AS measurement_count,
    AVG(metric_value) AS avg_value,
    MIN(metric_value) AS min_value,
    MAX(metric_value) AS max_value,
    MAX(timestamp) AS last_measurement
FROM performance_metrics
WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY metric_type, metric_name
ORDER BY metric_type, metric_name;

-- ==============================================
-- 성능 최적화 완료
-- ==============================================