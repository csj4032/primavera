-- Chapter 14 Spring Data JPA 테스트를 위한 데이터베이스 스키마

-- 기본 엔티티 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    granted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 소셜 로그인 연결 정보
CREATE TABLE IF NOT EXISTS user_connections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider_id VARCHAR(50) NOT NULL, -- google, facebook, github, kakao
    provider_user_id VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    profile_url VARCHAR(500),
    image_url VARCHAR(500),
    access_token TEXT,
    secret VARCHAR(255),
    refresh_token TEXT,
    expire_time BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_provider_user (provider_id, provider_user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_provider (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 게시글 테이블
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    summary TEXT,
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    comment_count BIGINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PUBLISHED', -- DRAFT, PUBLISHED, DELETED
    published_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    INDEX idx_published_at (published_at),
    INDEX idx_created_at (created_at),
    FULLTEXT idx_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 아티클 테이블 (계층형 구조 지원)
CREATE TABLE IF NOT EXISTS articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    reference BIGINT NOT NULL,
    step INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 0,
    author_id BIGINT NOT NULL,
    subject VARCHAR(200) NOT NULL,
    status TINYINT(3) NOT NULL DEFAULT 1,
    hit BIGINT NOT NULL DEFAULT 0,
    recommend BIGINT NOT NULL DEFAULT 0,
    disapprove BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES articles(id) ON DELETE CASCADE,
    INDEX idx_author_id (author_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_reference (reference),
    INDEX idx_step (step),
    INDEX idx_level (level),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 아티클 내용 테이블
CREATE TABLE IF NOT EXISTS article_contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    contents LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 댓글 테이블 (계층형 구조 지원)
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    parent_id BIGINT,
    author_id BIGINT NOT NULL,
    level INT NOT NULL DEFAULT 0,
    step INT NOT NULL DEFAULT 0,
    comment LONGTEXT NOT NULL,
    status TINYINT(3) NOT NULL DEFAULT 1,
    like_count BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_author_id (author_id),
    INDEX idx_level (level),
    INDEX idx_step (step),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 첨부파일 테이블
CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT,
    post_id BIGINT,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    download_count BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id),
    INDEX idx_post_id (post_id),
    INDEX idx_content_type (content_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Primavera 로그 테이블 (시스템 로그)
CREATE TABLE IF NOT EXISTS primavera_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_level VARCHAR(10) NOT NULL,
    logger_name VARCHAR(255) NOT NULL,
    message LONGTEXT NOT NULL,
    exception_message TEXT,
    stack_trace LONGTEXT,
    user_id BIGINT,
    session_id VARCHAR(100),
    request_uri VARCHAR(500),
    request_method VARCHAR(10),
    user_agent TEXT,
    client_ip VARCHAR(45),
    server_name VARCHAR(100),
    thread_name VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_log_level (log_level),
    INDEX idx_logger_name (logger_name),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_request_uri (request_uri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 시퀀스 테이블 (사용자 정의 시퀀스 관리)
CREATE TABLE IF NOT EXISTS sequences (
    name VARCHAR(50) PRIMARY KEY,
    current_value BIGINT NOT NULL DEFAULT 0,
    increment_value BIGINT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 초기 테스트 데이터 삽입
INSERT INTO users (email, password, nickname, status) VALUES 
('genius@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'Genius', 'ACTIVE'),
('admin@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'Administrator', 'ACTIVE'),
('user@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'User', 'ACTIVE'),
('manager@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'Manager', 'ACTIVE'),
('test@primavera.com', '{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 'TestUser', 'INACTIVE');

INSERT INTO roles (name, description) VALUES 
('ROLE_ADMIN', '시스템 관리자 권한'),
('ROLE_MANAGER', '매니저 권한'),
('ROLE_USER', '일반 사용자 권한'),
('ROLE_GUEST', '게스트 권한');

INSERT INTO user_roles (user_id, role_id, granted_by) VALUES 
(1, 1, NULL), -- genius -> ADMIN
(1, 2, NULL), -- genius -> MANAGER  
(1, 3, NULL), -- genius -> USER
(2, 1, 1),    -- admin -> ADMIN (granted by genius)
(2, 2, 1),    -- admin -> MANAGER (granted by genius)
(3, 3, 2),    -- user -> USER (granted by admin)
(4, 2, 1),    -- manager -> MANAGER (granted by genius)
(4, 3, 1),    -- manager -> USER (granted by genius)
(5, 4, 2);    -- test -> GUEST (granted by admin)

INSERT INTO user_connections (user_id, provider_id, provider_user_id, display_name, profile_url) VALUES 
(1, 'google', 'google_123456789', 'Genius Google', 'https://accounts.google.com/profile/genius'),
(1, 'github', 'github_genius', 'genius-dev', 'https://github.com/genius-dev'),
(2, 'kakao', 'kakao_987654321', '관리자', 'https://story.kakao.com/admin'),
(3, 'facebook', 'facebook_user123', '일반사용자', 'https://facebook.com/user123');

INSERT INTO posts (author_id, title, content, summary, status, published_at) VALUES 
(1, 'Spring Boot 3.0 새로운 기능들', 'Spring Boot 3.0에서 추가된 새로운 기능들을 살펴보겠습니다...', 'Spring Boot 3.0의 주요 업데이트 내용', 'PUBLISHED', NOW()),
(1, 'JPA 성능 최적화 가이드', 'JPA를 사용할 때 성능을 최적화하는 방법들을 정리했습니다...', 'JPA 성능 최적화 팁', 'PUBLISHED', NOW()),
(2, 'Docker와 Kubernetes 입문', 'Docker와 Kubernetes의 기본 개념부터 실습까지...', '컨테이너 기술 입문 가이드', 'PUBLISHED', NOW()),
(3, '프론트엔드 개발 트렌드', '2024년 프론트엔드 개발 트렌드를 분석해보겠습니다...', '최신 프론트엔드 기술 동향', 'DRAFT', NULL),
(4, 'DevOps 도구 비교 분석', '다양한 DevOps 도구들의 장단점을 비교분석합니다...', 'DevOps 도구 선택 가이드', 'PUBLISHED', NOW());

INSERT INTO articles (parent_id, reference, step, level, author_id, subject, status, hit) VALUES 
(0, 1, 0, 0, 1, 'Spring Boot 실습 가이드', 1, 150),
(0, 2, 0, 0, 2, 'JPA 관계 매핑 완전 정복', 1, 200),
(1, 1, 1, 1, 3, 'Re: Spring Boot 실습 가이드', 1, 50),
(1, 1, 2, 1, 4, 'Re: Spring Boot 실습 가이드 - 추가 질문', 1, 30),
(3, 1, 3, 2, 1, 'Re: Re: Spring Boot 실습 가이드', 1, 20),
(0, 3, 0, 0, 4, 'TestContainers 활용법', 1, 180),
(0, 4, 0, 0, 1, 'Docker 기반 개발 환경 구축', 1, 120);

INSERT INTO article_contents (article_id, contents) VALUES 
(1, 'Spring Boot는 스프링 애플리케이션을 빠르고 쉽게 개발할 수 있도록 도와주는 프레임워크입니다. 이 가이드에서는 실제 프로젝트를 통해 Spring Boot의 핵심 기능들을 학습해보겠습니다.'),
(2, 'JPA(Java Persistence API)의 관계 매핑은 객체지향 프로그래밍과 관계형 데이터베이스 사이의 패러다임 불일치를 해결하는 중요한 기술입니다. OneToOne, OneToMany, ManyToOne, ManyToMany 관계에 대해 상세히 알아보겠습니다.'),
(3, '좋은 가이드 감사합니다! 혹시 Spring Boot Security 설정 부분도 추가해주실 수 있나요?'),
(4, '저도 궁금한게 있는데, OAuth2 연동은 어떻게 하는지 알려주세요.'),
(5, 'Security와 OAuth2에 대한 내용은 다음 포스팅에서 다루도록 하겠습니다. 기다려주세요!'),
(6, 'TestContainers는 통합 테스트에서 실제 데이터베이스를 사용할 수 있게 해주는 도구입니다. Docker 컨테이너를 활용하여 격리된 테스트 환경을 제공합니다.'),
(7, 'Docker를 활용한 개발 환경 구축 방법을 단계별로 설명하겠습니다. 개발팀 전체가 동일한 환경에서 작업할 수 있도록 도와드립니다.');

INSERT INTO comments (article_id, parent_id, author_id, level, step, comment, status) VALUES 
(1, NULL, 2, 0, 0, '정말 유용한 가이드네요! 실습 따라하면서 많이 배웠습니다.', 1),
(1, NULL, 3, 0, 1, 'Spring Boot 버전별 차이점도 정리해주시면 좋겠어요.', 1),
(1, 1, 1, 1, 2, '감사합니다! 도움이 되셨다니 기쁩니다.', 1),
(1, 2, 1, 1, 3, '버전별 차이점은 별도 포스팅으로 준비해보겠습니다.', 1),
(2, NULL, 4, 0, 0, 'JPA 관계 매핑 부분이 항상 헷갈렸는데 이해가 잘 되네요.', 1),
(2, NULL, 1, 0, 1, '실무에서 자주 사용하는 패턴들 위주로 설명했습니다.', 1),
(6, NULL, 3, 0, 0, 'TestContainers 정말 편리하네요. 바로 적용해봤습니다!', 1);

INSERT INTO attachments (article_id, post_id, original_filename, stored_filename, file_path, file_size, content_type) VALUES 
(1, NULL, 'spring-boot-guide.pdf', '20240101_spring-boot-guide_abc123.pdf', '/uploads/articles/1/', 2048576, 'application/pdf'),
(2, NULL, 'jpa-examples.zip', '20240102_jpa-examples_def456.zip', '/uploads/articles/2/', 5242880, 'application/zip'),
(6, NULL, 'testcontainers-config.yml', '20240103_testcontainers-config_ghi789.yml', '/uploads/articles/6/', 4096, 'text/yaml'),
(NULL, 1, 'spring-boot-3-features.png', '20240104_spring-boot-3-features_jkl012.png', '/uploads/posts/1/', 1048576, 'image/png'),
(NULL, 3, 'docker-kubernetes-architecture.png', '20240105_docker-k8s-architecture_mno345.png', '/uploads/posts/3/', 2097152, 'image/png');

INSERT INTO primavera_logs (log_level, logger_name, message, user_id, session_id, request_uri, request_method, client_ip) VALUES 
('INFO', 'com.genius.primavera.PrimaveraApplication', 'Application started successfully', NULL, NULL, NULL, NULL, '127.0.0.1'),
('INFO', 'com.genius.primavera.interfaces.LoginController', 'User login attempt', 1, 'session123', '/login', 'POST', '192.168.1.100'),
('INFO', 'com.genius.primavera.interfaces.LoginController', 'User login successful', 1, 'session123', '/login', 'POST', '192.168.1.100'),
('WARN', 'com.genius.primavera.application.UserService', 'Failed login attempt for user: unknown@test.com', NULL, 'session456', '/login', 'POST', '192.168.1.200'),
('ERROR', 'com.genius.primavera.domain.repository.UserRepository', 'Database connection timeout', NULL, NULL, '/api/users', 'GET', '10.0.0.1'),
('DEBUG', 'com.genius.primavera.interfaces.ArticleController', 'Article retrieved successfully', 3, 'session789', '/articles/1', 'GET', '172.16.0.50');

INSERT INTO sequences (name, current_value, increment_value) VALUES 
('article_reference_seq', 10, 1),
('post_number_seq', 100, 1),
('user_id_seq', 1000, 1),
('attachment_seq', 50, 1);