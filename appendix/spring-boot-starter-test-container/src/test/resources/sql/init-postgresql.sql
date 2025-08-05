-- PostgreSQL 전용 초기화 스크립트

CREATE TABLE IF NOT EXISTS test_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 병렬 테스트용 카운터 테이블
CREATE TABLE IF NOT EXISTS test_counter (
    id INT PRIMARY KEY,
    value INT NOT NULL,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 병렬 테스트용 글로벌 상태 테이블
CREATE TABLE IF NOT EXISTS test_global_state (
    key_name VARCHAR(50) PRIMARY KEY,
    value_data TEXT,
    last_updated_by VARCHAR(100),
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 병렬 테스트용 실행 로그 테이블
CREATE TABLE IF NOT EXISTS test_execution_log (
    id BIGSERIAL PRIMARY KEY,
    test_name VARCHAR(200),
    thread_name VARCHAR(100),
    container_info TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    execution_order INT,
    status VARCHAR(20) DEFAULT 'RUNNING'
);

-- 병렬 테스트용 공유 리소스 카운터 테이블
CREATE TABLE IF NOT EXISTS shared_resource_counter (
    counter_name VARCHAR(50) PRIMARY KEY,
    counter_value INT DEFAULT 0,
    last_updated_by VARCHAR(100),
    update_count INT DEFAULT 0
);

-- 초기 데이터 삽입
INSERT INTO test_users (name, email) VALUES 
('Test User 1', 'test1@example.com'),
('Test User 2', 'test2@example.com');

-- 카운터 초기값 설정 (PostgreSQL용 UPSERT 문법)
INSERT INTO test_counter (id, value, updated_by) VALUES (1, 0, 'INIT')
ON CONFLICT (id) DO NOTHING;

-- 공유 리소스 카운터 초기값
INSERT INTO shared_resource_counter (counter_name, counter_value) VALUES ('TEST_COUNTER', 0)
ON CONFLICT (counter_name) DO NOTHING;