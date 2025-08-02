-- ==============================================
-- Primavera Chapter 14-17 - JPA Environment Database
-- JPA 고급 매핑, 리액티브 프로그래밍, 파일처리, CI/CD
-- MariaDB 11.4.7 - JPA 및 고급 기능 시스템
-- ==============================================

-- 기본 데이터베이스 설정
ALTER DATABASE primavera CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==============================================
-- JPA 고급 및 운영 데이터베이스 생성
-- ==============================================

-- 1. JPA 고급 매핑 데이터베이스
CREATE DATABASE IF NOT EXISTS primavera_jpa_advanced CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 2. JPA 게시판 데이터베이스 (파일처리 포함)
CREATE DATABASE IF NOT EXISTS primavera_jpa_board CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==============================================
-- 권한 설정
-- ==============================================

GRANT ALL PRIVILEGES ON primavera.* TO 'primavera'@'%' IDENTIFIED BY 'primavera';
GRANT ALL PRIVILEGES ON primavera_jpa_advanced.* TO 'primavera'@'%' IDENTIFIED BY 'primavera';
GRANT ALL PRIVILEGES ON primavera_jpa_board.* TO 'primavera'@'%' IDENTIFIED BY 'primavera';

FLUSH PRIVILEGES;

-- ==============================================
-- JPA 고급 매핑 데이터베이스 (primavera_jpa_advanced)
-- Chapter 14-15용 JPA 고급 기능 및 리액티브 프로그래밍
-- ==============================================

USE primavera_jpa_advanced;

-- 기본 엔티티: 회사 (Company)
CREATE TABLE IF NOT EXISTS companies
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    business_number VARCHAR(20) UNIQUE,
    email        VARCHAR(100),
    phone        VARCHAR(20),
    address      TEXT,
    website      VARCHAR(200),
    industry     VARCHAR(100),
    employee_count INT DEFAULT 0,
    founded_date DATE,
    status       VARCHAR(20) DEFAULT 'ACTIVE',
    created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_companies_status (status),
    INDEX idx_companies_industry (industry),
    INDEX idx_companies_business_number (business_number)
);

-- 부서 엔티티 (Department) - 회사와 일대다 관계
CREATE TABLE IF NOT EXISTS departments
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    budget      DECIMAL(15, 2) DEFAULT 0,
    manager_id  BIGINT,
    parent_id   BIGINT, -- 자기 참조 (부서 계층 구조)
    level       INT      DEFAULT 1,
    sort_order  INT      DEFAULT 0,
    status      VARCHAR(20) DEFAULT 'ACTIVE',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES departments (id) ON DELETE SET NULL,
    INDEX idx_departments_company_id (company_id),
    INDEX idx_departments_parent_id (parent_id),
    INDEX idx_departments_status (status)
);

-- 직원 엔티티 (Employee) - 부서와 다대일 관계
CREATE TABLE IF NOT EXISTS employees
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id    BIGINT       NOT NULL,
    department_id BIGINT,
    employee_number VARCHAR(20) UNIQUE NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    password      VARCHAR(255),
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    full_name     VARCHAR(100) GENERATED ALWAYS AS (CONCAT(first_name, ' ', last_name)) STORED,
    phone         VARCHAR(20),
    mobile        VARCHAR(20),
    birth_date    DATE,
    hire_date     DATE         NOT NULL,
    resignation_date DATE,
    position      VARCHAR(50),
    level         VARCHAR(20),
    salary        DECIMAL(12, 2),
    manager_id    BIGINT, -- 자기 참조 (직속 상관)
    profile_image VARCHAR(500),
    address       TEXT,
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(20),
    status        VARCHAR(20) DEFAULT 'ACTIVE',
    created_at    DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL,
    FOREIGN KEY (manager_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_employees_company_id (company_id),
    INDEX idx_employees_department_id (department_id),
    INDEX idx_employees_manager_id (manager_id),
    INDEX idx_employees_employee_number (employee_number),
    INDEX idx_employees_email (email),
    INDEX idx_employees_status (status),
    INDEX idx_employees_hire_date (hire_date)
);

-- 부서 매니저 관계 설정 (외래키 추가)
ALTER TABLE departments 
ADD CONSTRAINT fk_departments_manager 
FOREIGN KEY (manager_id) REFERENCES employees (id) ON DELETE SET NULL;

-- 프로젝트 엔티티 (Project)
CREATE TABLE IF NOT EXISTS projects
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id   BIGINT       NOT NULL,
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    project_code VARCHAR(20) UNIQUE NOT NULL,
    budget       DECIMAL(15, 2) DEFAULT 0,
    actual_cost  DECIMAL(15, 2) DEFAULT 0,
    start_date   DATE,
    end_date     DATE,
    status       VARCHAR(20) DEFAULT 'PLANNING', -- PLANNING, ACTIVE, COMPLETED, CANCELLED, ON_HOLD
    priority     VARCHAR(20) DEFAULT 'MEDIUM',   -- LOW, MEDIUM, HIGH, CRITICAL
    progress     INT         DEFAULT 0, -- 0-100%
    manager_id   BIGINT,
    client_name  VARCHAR(200),
    client_contact VARCHAR(100),
    created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    FOREIGN KEY (manager_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_projects_company_id (company_id),
    INDEX idx_projects_manager_id (manager_id),
    INDEX idx_projects_status (status),
    INDEX idx_projects_start_date (start_date),
    INDEX idx_projects_project_code (project_code)
);

-- 프로젝트 참여자 매핑 테이블 (다대다 관계)
CREATE TABLE IF NOT EXISTS project_members
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    role        VARCHAR(50) DEFAULT 'MEMBER', -- MANAGER, LEADER, MEMBER, CONSULTANT
    join_date   DATE        DEFAULT (CURDATE()),
    leave_date  DATE,
    allocation_rate DECIMAL(5, 2) DEFAULT 100.00, -- 투입률 (%)
    hourly_rate DECIMAL(10, 2),
    status      VARCHAR(20) DEFAULT 'ACTIVE',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    UNIQUE KEY uk_project_member (project_id, employee_id),
    INDEX idx_project_members_project_id (project_id),
    INDEX idx_project_members_employee_id (employee_id),
    INDEX idx_project_members_status (status)
);

-- 태스크 엔티티 (Task) - 프로젝트와 일대다 관계
CREATE TABLE IF NOT EXISTS tasks
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT       NOT NULL,
    parent_id   BIGINT, -- 상위 태스크 (계층 구조)
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    task_type   VARCHAR(50) DEFAULT 'TASK', -- EPIC, STORY, TASK, BUG, IMPROVEMENT
    priority    VARCHAR(20) DEFAULT 'MEDIUM',
    status      VARCHAR(20) DEFAULT 'TODO', -- TODO, IN_PROGRESS, REVIEW, DONE, CANCELLED
    assignee_id BIGINT,
    reporter_id BIGINT,
    reviewer_id BIGINT,
    story_points INT,
    estimated_hours DECIMAL(6, 2),
    actual_hours DECIMAL(6, 2) DEFAULT 0,
    progress    INT         DEFAULT 0,
    start_date  DATE,
    due_date    DATE,
    completed_date DATE,
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES tasks (id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES employees (id) ON DELETE SET NULL,
    FOREIGN KEY (reporter_id) REFERENCES employees (id) ON DELETE SET NULL,
    FOREIGN KEY (reviewer_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_tasks_project_id (project_id),
    INDEX idx_tasks_parent_id (parent_id),
    INDEX idx_tasks_assignee_id (assignee_id),
    INDEX idx_tasks_status (status),
    INDEX idx_tasks_due_date (due_date)
);

-- 시간 로그 엔티티 (TimeLog) - 직원, 프로젝트, 태스크와 관계
CREATE TABLE IF NOT EXISTS time_logs
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT        NOT NULL,
    project_id  BIGINT        NOT NULL,
    task_id     BIGINT,
    work_date   DATE          NOT NULL,
    start_time  TIME,
    end_time    TIME,
    hours       DECIMAL(4, 2) NOT NULL,
    description TEXT,
    is_billable BOOLEAN DEFAULT TRUE,
    hourly_rate DECIMAL(10, 2),
    total_cost  DECIMAL(12, 2) GENERATED ALWAYS AS (hours * IFNULL(hourly_rate, 0)) STORED,
    status      VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, SUBMITTED, APPROVED, REJECTED
    submitted_at DATETIME,
    approved_at DATETIME,
    approved_by BIGINT,
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE SET NULL,
    FOREIGN KEY (approved_by) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_time_logs_employee_id (employee_id),
    INDEX idx_time_logs_project_id (project_id),
    INDEX idx_time_logs_task_id (task_id),
    INDEX idx_time_logs_work_date (work_date),
    INDEX idx_time_logs_status (status)
);

-- 주소 엔티티 (Address) - Embedded 타입 시뮬레이션
CREATE TABLE IF NOT EXISTS addresses
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(20) NOT NULL, -- COMPANY, EMPLOYEE
    entity_id   BIGINT      NOT NULL,
    address_type VARCHAR(20) DEFAULT 'PRIMARY', -- PRIMARY, BILLING, SHIPPING, HOME, WORK
    country     VARCHAR(2)  DEFAULT 'KR',
    state       VARCHAR(50),
    city        VARCHAR(100),
    district    VARCHAR(100),
    street      VARCHAR(200),
    detail      VARCHAR(200),
    postal_code VARCHAR(20),
    latitude    DECIMAL(10, 8),
    longitude   DECIMAL(11, 8),
    is_primary  BOOLEAN     DEFAULT FALSE,
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_addresses_entity (entity_type, entity_id),
    INDEX idx_addresses_type (address_type),
    INDEX idx_addresses_location (latitude, longitude)
);

-- 연락처 정보 엔티티 (ContactInfo)
CREATE TABLE IF NOT EXISTS contact_infos
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type  VARCHAR(20) NOT NULL, -- COMPANY, EMPLOYEE
    entity_id    BIGINT      NOT NULL,
    contact_type VARCHAR(20) NOT NULL, -- EMAIL, PHONE, MOBILE, FAX, SKYPE, LINKEDIN
    value        VARCHAR(200) NOT NULL,
    label        VARCHAR(50),
    is_primary   BOOLEAN     DEFAULT FALSE,
    is_public    BOOLEAN     DEFAULT FALSE,
    created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_contact_infos_entity (entity_type, entity_id),
    INDEX idx_contact_infos_type (contact_type),
    INDEX idx_contact_infos_value (value)
);

-- ==============================================
-- JPA 게시판 데이터베이스 (primavera_jpa_board)
-- Chapter 16-17용 파일처리, 모니터링, CI/CD
-- ==============================================

USE primavera_jpa_board;

-- 사용자 엔티티 (JPA 버전)
CREATE TABLE IF NOT EXISTS users
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    email            VARCHAR(100) UNIQUE NOT NULL,
    password         VARCHAR(255),
    nickname         VARCHAR(50)         NOT NULL,
    first_name       VARCHAR(50),
    last_name        VARCHAR(50),
    phone            VARCHAR(20),
    profile_image_id BIGINT, -- 파일 엔티티 참조
    provider         VARCHAR(20) DEFAULT 'LOCAL',
    provider_id      VARCHAR(100),
    email_verified   BOOLEAN     DEFAULT FALSE,
    status           VARCHAR(20) DEFAULT 'ACTIVE',
    last_login_at    DATETIME,
    created_at       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version          BIGINT      DEFAULT 0, -- JPA Optimistic Locking
    
    INDEX idx_users_email (email),
    INDEX idx_users_nickname (nickname),
    INDEX idx_users_status (status)
);

-- 파일 엔티티 (FileEntity) - 파일 처리 시스템
CREATE TABLE IF NOT EXISTS file_entities
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name    VARCHAR(255) NOT NULL,
    stored_name      VARCHAR(255) NOT NULL,
    file_path        VARCHAR(500) NOT NULL,
    file_size        BIGINT       NOT NULL,
    content_type     VARCHAR(100),
    file_extension   VARCHAR(10),
    file_hash        VARCHAR(64) UNIQUE, -- SHA-256 해시 (중복 파일 방지)
    storage_type     VARCHAR(20) DEFAULT 'LOCAL', -- LOCAL, S3, GCS, AZURE
    bucket_name      VARCHAR(100),
    storage_path     VARCHAR(500),
    thumbnail_path   VARCHAR(500),
    is_image         BOOLEAN     DEFAULT FALSE,
    image_width      INT,
    image_height     INT,
    is_processed     BOOLEAN     DEFAULT FALSE, -- 이미지 처리 완료 여부
    download_count   INT         DEFAULT 0,
    access_level     VARCHAR(20) DEFAULT 'PRIVATE', -- PUBLIC, PRIVATE, RESTRICTED
    owner_id         BIGINT,
    expires_at       DATETIME,
    status           VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, DELETED, QUARANTINE
    virus_scan_result VARCHAR(20) DEFAULT 'PENDING', -- PENDING, CLEAN, INFECTED
    created_at       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version          BIGINT      DEFAULT 0,
    
    FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_file_entities_file_hash (file_hash),
    INDEX idx_file_entities_owner_id (owner_id),
    INDEX idx_file_entities_storage_type (storage_type),
    INDEX idx_file_entities_content_type (content_type),
    INDEX idx_file_entities_status (status),
    INDEX idx_file_entities_created_at (created_at)
);

-- 프로파일 이미지 외래키 추가
ALTER TABLE users 
ADD CONSTRAINT fk_users_profile_image 
FOREIGN KEY (profile_image_id) REFERENCES file_entities (id) ON DELETE SET NULL;

-- 게시글 엔티티 (JPA 버전)
CREATE TABLE IF NOT EXISTS articles
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    content         LONGTEXT     NOT NULL,
    content_type    VARCHAR(20) DEFAULT 'HTML',
    author_id       BIGINT       NOT NULL,
    thumbnail_id    BIGINT, -- 썸네일 파일 참조
    view_count      INT         DEFAULT 0,
    like_count      INT         DEFAULT 0,
    comment_count   INT         DEFAULT 0,
    status          VARCHAR(20) DEFAULT 'PUBLISHED',
    is_featured     BOOLEAN     DEFAULT FALSE,
    allow_comments  BOOLEAN     DEFAULT TRUE,
    seo_title       VARCHAR(200),
    seo_description VARCHAR(500),
    seo_keywords    VARCHAR(200),
    published_at    DATETIME,
    created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version         BIGINT      DEFAULT 0,
    
    FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (thumbnail_id) REFERENCES file_entities (id) ON DELETE SET NULL,
    INDEX idx_articles_author_id (author_id),
    INDEX idx_articles_status (status),
    INDEX idx_articles_published_at (published_at),
    INDEX idx_articles_is_featured (is_featured),
    FULLTEXT INDEX ft_articles_content (title, content, seo_keywords)
);

-- 게시글 첨부파일 매핑 테이블
CREATE TABLE IF NOT EXISTS article_attachments
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    file_id    BIGINT NOT NULL,
    sort_order INT DEFAULT 0,
    description VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES file_entities (id) ON DELETE CASCADE,
    UNIQUE KEY uk_article_file (article_id, file_id),
    INDEX idx_article_attachments_article_id (article_id),
    INDEX idx_article_attachments_file_id (file_id)
);

-- 댓글 엔티티 (JPA 버전)
CREATE TABLE IF NOT EXISTS comments
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT   NOT NULL,
    parent_id  BIGINT,
    author_id  BIGINT   NOT NULL,
    content    TEXT     NOT NULL,
    status     VARCHAR(20) DEFAULT 'ACTIVE',
    depth      INT      DEFAULT 0,
    sort_order INT      DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version    BIGINT   DEFAULT 0,
    
    FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments (id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_comments_article_id (article_id),
    INDEX idx_comments_parent_id (parent_id),
    INDEX idx_comments_author_id (author_id)
);

-- 파일 처리 작업 큐 테이블
CREATE TABLE IF NOT EXISTS file_process_jobs
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id     BIGINT      NOT NULL,
    job_type    VARCHAR(50) NOT NULL, -- THUMBNAIL, RESIZE, COMPRESS, VIRUS_SCAN, EXTRACT_METADATA
    status      VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    priority    INT         DEFAULT 5, -- 1(높음) ~ 10(낮음)
    parameters  JSON,       -- 작업 매개변수
    result      JSON,       -- 작업 결과
    error_message TEXT,
    attempts    INT         DEFAULT 0,
    max_attempts INT        DEFAULT 3,
    started_at  DATETIME,
    completed_at DATETIME,
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (file_id) REFERENCES file_entities (id) ON DELETE CASCADE,
    INDEX idx_file_process_jobs_file_id (file_id),
    INDEX idx_file_process_jobs_status (status),
    INDEX idx_file_process_jobs_priority (priority),
    INDEX idx_file_process_jobs_created_at (created_at)
);

-- 시스템 메트릭스 테이블 (모니터링용)
CREATE TABLE IF NOT EXISTS system_metrics
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_type VARCHAR(50)  NOT NULL, -- COUNTER, GAUGE, HISTOGRAM, SUMMARY
    value       DECIMAL(20, 6) NOT NULL,
    tags        JSON,        -- 메트릭 태그 (JSON 형태)
    timestamp   DATETIME     NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_system_metrics_name_time (metric_name, timestamp),
    INDEX idx_system_metrics_type (metric_type),
    INDEX idx_system_metrics_timestamp (timestamp)
);

-- 애플리케이션 로그 테이블
CREATE TABLE IF NOT EXISTS application_logs
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    level       VARCHAR(10)  NOT NULL, -- ERROR, WARN, INFO, DEBUG, TRACE
    logger_name VARCHAR(200) NOT NULL,
    message     TEXT         NOT NULL,
    exception   TEXT,
    mdc         JSON,        -- Mapped Diagnostic Context
    thread_name VARCHAR(100),
    user_id     BIGINT,
    session_id  VARCHAR(100),
    request_id  VARCHAR(100),
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(500),
    timestamp   DATETIME NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_application_logs_level (level),
    INDEX idx_application_logs_logger (logger_name),
    INDEX idx_application_logs_timestamp (timestamp),
    INDEX idx_application_logs_user_id (user_id),
    INDEX idx_application_logs_request_id (request_id)
);

-- 배포 히스토리 테이블 (CI/CD용)
CREATE TABLE IF NOT EXISTS deployment_history
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    application     VARCHAR(100) NOT NULL,
    version         VARCHAR(50)  NOT NULL,
    environment     VARCHAR(20)  NOT NULL, -- DEV, STAGE, PROD
    git_commit      VARCHAR(40),
    git_branch      VARCHAR(100),
    git_tag         VARCHAR(50),
    deployed_by     VARCHAR(100),
    deployment_type VARCHAR(50) DEFAULT 'NORMAL', -- NORMAL, ROLLBACK, HOTFIX
    status          VARCHAR(20) DEFAULT 'DEPLOYING', -- DEPLOYING, SUCCESS, FAILED, ROLLBACK
    start_time      DATETIME     NOT NULL,
    end_time        DATETIME,
    duration_seconds INT,
    build_number    VARCHAR(50),
    build_url       VARCHAR(500),
    release_notes   TEXT,
    rollback_version VARCHAR(50),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_deployment_history_app_env (application, environment),
    INDEX idx_deployment_history_status (status),
    INDEX idx_deployment_history_start_time (start_time),
    INDEX idx_deployment_history_git_commit (git_commit)
);

-- ==============================================
-- 초기 데이터 삽입
-- ==============================================

-- JPA 고급 데이터베이스 초기 데이터
USE primavera_jpa_advanced;

-- 회사 데이터
INSERT IGNORE INTO companies (name, business_number, email, phone, industry, employee_count, founded_date) VALUES 
('Primavera Technologies', '123-45-67890', 'info@primavera.com', '02-1234-5678', 'Software Development', 150, '2020-01-15'),
('Innovation Corp', '987-65-43210', 'contact@innovation.com', '02-9876-5432', 'IT Consulting', 80, '2019-05-20'),
('Digital Solutions', '456-78-90123', 'hello@digital.com', '02-4567-8901', 'Digital Marketing', 45, '2021-03-10');

-- 부서 데이터
INSERT IGNORE INTO departments (company_id, name, description, budget, level, sort_order) VALUES 
(1, '개발본부', '소프트웨어 개발 총괄', 5000000000, 1, 1),
(1, '백엔드개발팀', 'Spring Boot, 마이크로서비스 개발', 2000000000, 2, 1),
(1, '프론트엔드개발팀', 'React, Vue.js 개발', 1500000000, 2, 2),
(1, '데브옵스팀', 'CI/CD, 인프라 관리', 1000000000, 2, 3),
(1, '영업본부', '영업 및 마케팅', 2000000000, 1, 2),
(1, '인사총무팀', '인사 및 총무 업무', 800000000, 1, 3);

-- 직원 데이터
INSERT IGNORE INTO employees (company_id, department_id, employee_number, email, first_name, last_name, phone, hire_date, position, level, salary) VALUES 
(1, 1, 'EMP001', 'cto@primavera.com', 'John', 'Smith', '010-1111-1111', '2020-01-20', 'CTO', 'EXECUTIVE', 150000000),
(1, 2, 'EMP002', 'backend.lead@primavera.com', 'Jane', 'Doe', '010-2222-2222', '2020-02-01', 'Backend Team Lead', 'SENIOR', 100000000),
(1, 3, 'EMP003', 'frontend.lead@primavera.com', 'Bob', 'Johnson', '010-3333-3333', '2020-02-15', 'Frontend Team Lead', 'SENIOR', 95000000),
(1, 4, 'EMP004', 'devops.lead@primavera.com', 'Alice', 'Wilson', '010-4444-4444', '2020-03-01', 'DevOps Team Lead', 'SENIOR', 105000000),
(1, 2, 'EMP005', 'developer1@primavera.com', 'Charlie', 'Brown', '010-5555-5555', '2020-06-01', 'Senior Developer', 'SENIOR', 85000000),
(1, 2, 'EMP006', 'developer2@primavera.com', 'Diana', 'Davis', '010-6666-6666', '2021-01-15', 'Developer', 'JUNIOR', 65000000);

-- 매니저 관계 설정
UPDATE departments SET manager_id = 1 WHERE id = 1; -- 개발본부 CTO
UPDATE departments SET manager_id = 2 WHERE id = 2; -- 백엔드팀 리드
UPDATE departments SET manager_id = 3 WHERE id = 3; -- 프론트엔드팀 리드
UPDATE departments SET manager_id = 4 WHERE id = 4; -- 데브옵스팀 리드

UPDATE employees SET manager_id = 1 WHERE id IN (2, 3, 4); -- 팀리드들의 상관은 CTO
UPDATE employees SET manager_id = 2 WHERE id IN (5, 6); -- 개발자들의 상관은 백엔드 리드

-- 프로젝트 데이터
INSERT IGNORE INTO projects (company_id, name, description, project_code, budget, start_date, end_date, status, manager_id, client_name) VALUES 
(1, 'Primavera Platform V2', 'Spring Boot 기반 통합 플랫폼 개발', 'PROJ001', 800000000, '2024-01-01', '2024-12-31', 'ACTIVE', 2, 'Internal Project'),
(1, 'E-Commerce Solution', '대규모 전자상거래 플랫폼 구축', 'PROJ002', 1200000000, '2024-03-01', '2024-11-30', 'ACTIVE', 2, 'Commerce Corp'),
(1, 'Mobile App Backend', '모바일 앱 백엔드 API 개발', 'PROJ003', 400000000, '2024-06-01', '2024-09-30', 'PLANNING', 3, 'Mobile Startup');

-- 프로젝트 참여자
INSERT IGNORE INTO project_members (project_id, employee_id, role, allocation_rate, hourly_rate) VALUES 
(1, 2, 'MANAGER', 80.00, 120000),
(1, 5, 'LEADER', 100.00, 95000),
(1, 6, 'MEMBER', 100.00, 75000),
(2, 2, 'MANAGER', 50.00, 120000),
(2, 5, 'MEMBER', 80.00, 95000),
(3, 3, 'MANAGER', 60.00, 110000);

-- 태스크 데이터
INSERT IGNORE INTO tasks (project_id, title, description, task_type, priority, status, assignee_id, reporter_id, story_points, estimated_hours, due_date) VALUES 
(1, 'API 설계 및 개발', 'RESTful API 설계 및 구현', 'STORY', 'HIGH', 'IN_PROGRESS', 5, 2, 8, 40.0, '2024-08-15'),
(1, '데이터베이스 스키마 설계', 'JPA 엔티티 및 데이터베이스 설계', 'TASK', 'HIGH', 'DONE', 2, 2, 5, 20.0, '2024-07-30'),
(1, '보안 인증 구현', 'Spring Security 기반 인증/인가', 'STORY', 'MEDIUM', 'TODO', 6, 2, 13, 60.0, '2024-09-01'),
(2, '상품 관리 시스템', '상품 CRUD 및 검색 기능', 'EPIC', 'HIGH', 'ACTIVE', 5, 2, 21, 100.0, '2024-10-15');

-- JPA 게시판 데이터베이스 초기 데이터
USE primavera_jpa_board;

-- 사용자 데이터
INSERT IGNORE INTO users (email, password, nickname, first_name, last_name, phone, email_verified) VALUES 
('admin@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin', '관리자', '시스템', '010-0000-0000', TRUE),
('writer@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Writer', '작성자', '사용자', '010-1111-2222', TRUE),
('reader@primavera.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Reader', '독자', '사용자', '010-3333-4444', TRUE);

-- 파일 엔티티 데이터 (샘플)
INSERT IGNORE INTO file_entities (original_name, stored_name, file_path, file_size, content_type, file_extension, file_hash, storage_type, is_image, owner_id, access_level, virus_scan_result) VALUES 
('spring-boot-logo.png', '20240802_spring_boot_logo_abc123.png', '/uploads/images/2024/08/02/', 45678, 'image/png', 'png', 'a1b2c3d4e5f6789...', 'LOCAL', TRUE, 1, 'PUBLIC', 'CLEAN'),
('document.pdf', '20240802_document_def456.pdf', '/uploads/docs/2024/08/02/', 234567, 'application/pdf', 'pdf', 'f6e5d4c3b2a1098...', 'LOCAL', FALSE, 2, 'PRIVATE', 'CLEAN'),
('profile.jpg', '20240802_profile_ghi789.jpg', '/uploads/profiles/2024/08/02/', 123456, 'image/jpeg', 'jpg', '789abc123def456...', 'LOCAL', TRUE, 3, 'PRIVATE', 'CLEAN');

-- 게시글 데이터
INSERT IGNORE INTO articles (title, content, author_id, thumbnail_id, status, is_featured, seo_title, seo_description, published_at) VALUES 
('JPA 고급 매핑 완벽 가이드', '<h1>JPA 고급 매핑 기법</h1><p>연관관계 매핑, 상속 매핑, 복합키 매핑 등 JPA의 고급 기능들을 상세히 알아봅시다.</p>', 2, 1, 'PUBLISHED', TRUE, 'JPA 고급 매핑 - 연관관계와 상속', 'JPA 고급 매핑 기법에 대한 완벽한 가이드', NOW()),
('Spring Boot 파일 업로드 시스템', '<h2>파일 업로드 구현</h2><p>Spring Boot에서 파일 업로드, 썸네일 생성, 바이러스 검사까지 구현해봅시다.</p>', 1, 2, 'PUBLISHED', FALSE, 'Spring Boot 파일 업로드', 'Spring Boot 파일 업로드 시스템 구현 가이드', NOW()),
('리액티브 프로그래밍 입문', '<p>Spring WebFlux를 활용한 리액티브 프로그래밍의 기초를 알아봅시다.</p>', 2, NULL, 'PUBLISHED', FALSE, NULL, NULL, NOW());

-- 댓글 데이터
INSERT IGNORE INTO comments (article_id, author_id, content) VALUES 
(1, 3, '정말 유용한 정보네요! JPA 매핑에 대해 많이 배웠습니다.'),
(1, 1, '실무에서 자주 사용하는 패턴들이 잘 정리되어 있어요.'),
(2, 3, '파일 업로드 보안 부분이 특히 도움이 되었습니다.'),
(3, 1, '리액티브 프로그래밍은 정말 흥미로운 분야죠!');

-- 게시글 첨부파일 매핑
INSERT IGNORE INTO article_attachments (article_id, file_id, sort_order, description) VALUES 
(1, 1, 1, 'JPA 매핑 다이어그램'),
(2, 2, 1, '파일 업로드 설계 문서');

-- 파일 처리 작업 데이터
INSERT IGNORE INTO file_process_jobs (file_id, job_type, status, priority, parameters, result) VALUES 
(1, 'THUMBNAIL', 'COMPLETED', 3, '{"width": 200, "height": 200}', '{"thumbnail_path": "/uploads/thumbnails/20240802_spring_boot_logo_thumb.png"}'),
(3, 'THUMBNAIL', 'COMPLETED', 3, '{"width": 150, "height": 150}', '{"thumbnail_path": "/uploads/thumbnails/20240802_profile_thumb.jpg"}'),
(2, 'VIRUS_SCAN', 'COMPLETED', 1, '{}', '{"scan_result": "CLEAN", "scan_engine": "ClamAV"}');

-- 시스템 메트릭스 샘플 데이터
INSERT IGNORE INTO system_metrics (metric_name, metric_type, value, tags, timestamp) VALUES 
('http_requests_total', 'COUNTER', 1567.0, '{"method": "GET", "status": "200"}', NOW()),
('jvm_memory_used_bytes', 'GAUGE', 512000000.0, '{"area": "heap", "id": "G1 Old Gen"}', NOW()),
('file_upload_duration_seconds', 'HISTOGRAM', 0.245, '{"status": "success"}', NOW());

-- 배포 히스토리 샘플 데이터
INSERT IGNORE INTO deployment_history (application, version, environment, git_commit, git_branch, deployed_by, status, start_time, end_time, duration_seconds, build_number) VALUES 
('primavera-api', 'v2.1.0', 'PROD', 'a1b2c3d4e5f6', 'main', 'devops@primavera.com', 'SUCCESS', '2024-08-01 10:00:00', '2024-08-01 10:05:30', 330, '245'),
('primavera-web', 'v2.1.0', 'PROD', 'f6e5d4c3b2a1', 'main', 'devops@primavera.com', 'SUCCESS', '2024-08-01 10:10:00', '2024-08-01 10:12:15', 135, '246');

-- ==============================================
-- 종료 메시지
-- ==============================================

SELECT 'Primavera JPA Advanced Environment Database Initialization Completed!' as STATUS;