-- Chapter 13 JPA 관계 매핑 테스트를 위한 종합 데이터베이스 스키마

-- ===========================================
-- OneToOne 관계 테이블들
-- ===========================================

-- Member-Address OneToOne 관계
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'South Korea',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Product-Serial OneToOne 관계
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS serials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    manufacture_date DATE NOT NULL,
    warranty_period_months INT DEFAULT 12,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Article-Content OneToOne 관계
CREATE TABLE IF NOT EXISTS articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    published_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'DRAFT'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    body LONGTEXT NOT NULL,
    word_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Book-ISBN OneToOne 관계
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    publication_date DATE,
    pages INT DEFAULT 0,
    price DECIMAL(8,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS isbns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    isbn_10 VARCHAR(10),
    isbn_13 VARCHAR(13) UNIQUE NOT NULL,
    assigned_date DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    INDEX idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- OneToMany 관계 테이블들
-- ===========================================

-- Customer-Contact OneToMany 관계
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    registration_date DATE DEFAULT (CURRENT_DATE),
    status VARCHAR(20) DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    contact_type VARCHAR(50) NOT NULL, -- EMAIL, PHONE, ADDRESS
    contact_value VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_contact_type (contact_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Professor-Student OneToMany 관계
CREATE TABLE IF NOT EXISTS professors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    office VARCHAR(50),
    hire_date DATE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professor_id BIGINT,
    student_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    major VARCHAR(100) NOT NULL,
    grade INT NOT NULL CHECK (grade BETWEEN 1 AND 4),
    email VARCHAR(100) UNIQUE NOT NULL,
    enrollment_date DATE NOT NULL,
    FOREIGN KEY (professor_id) REFERENCES professors(id) ON DELETE SET NULL,
    INDEX idx_professor_id (professor_id),
    INDEX idx_student_number (student_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- ManyToOne 관계 테이블들
-- ===========================================

-- Department-Employee ManyToOne 관계
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) UNIQUE NOT NULL,
    manager_name VARCHAR(100),
    budget DECIMAL(15,2) DEFAULT 0.00,
    established_date DATE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    employee_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    salary DECIMAL(10,2) NOT NULL,
    hire_date DATE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    INDEX idx_department_id (department_id),
    INDEX idx_employee_number (employee_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Team-Player ManyToOne 관계
CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    founded_year INT NOT NULL,
    league VARCHAR(100) NOT NULL,
    budget DECIMAL(15,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT,
    jersey_number INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    position VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    nationality VARCHAR(100) DEFAULT 'South Korea',
    salary DECIMAL(12,2) DEFAULT 0.00,
    contract_start_date DATE,
    contract_end_date DATE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL,
    INDEX idx_team_id (team_id),
    INDEX idx_position (position),
    UNIQUE KEY unique_team_jersey (team_id, jersey_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- ManyToMany 관계 테이블들
-- ===========================================

-- Buyer-Seller ManyToMany 관계 (Contract 중간 테이블)
CREATE TABLE IF NOT EXISTS buyers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    company VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    registration_date DATE DEFAULT (CURRENT_DATE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sellers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    company VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    registration_date DATE DEFAULT (CURRENT_DATE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    contract_number VARCHAR(50) UNIQUE NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KRW',
    contract_date DATE NOT NULL,
    delivery_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES buyers(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE CASCADE,
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_contract_date (contract_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Publisher-Subscriber ManyToMany 관계
CREATE TABLE IF NOT EXISTS publishers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    website VARCHAR(255),
    established_date DATE,
    country VARCHAR(100) DEFAULT 'South Korea'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS subscribers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    subscription_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    subscription_type VARCHAR(50) DEFAULT 'BASIC'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS publisher_subscribers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    publisher_id BIGINT NOT NULL,
    subscriber_id BIGINT NOT NULL,
    subscription_start_date DATE NOT NULL,
    subscription_end_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (publisher_id) REFERENCES publishers(id) ON DELETE CASCADE,
    FOREIGN KEY (subscriber_id) REFERENCES subscribers(id) ON DELETE CASCADE,
    UNIQUE KEY unique_publisher_subscriber (publisher_id, subscriber_id),
    INDEX idx_publisher_id (publisher_id),
    INDEX idx_subscriber_id (subscriber_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Origin-Destination ManyToMany 관계
CREATE TABLE IF NOT EXISTS origins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) UNIQUE NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    airport_code VARCHAR(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS destinations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) UNIQUE NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    airport_code VARCHAR(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS origin_destinations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    origin_id BIGINT NOT NULL,
    destination_id BIGINT NOT NULL,
    distance_km INT NOT NULL DEFAULT 0,
    flight_time_minutes INT NOT NULL DEFAULT 0,
    is_direct_flight BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (origin_id) REFERENCES origins(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
    UNIQUE KEY unique_origin_destination (origin_id, destination_id),
    INDEX idx_origin_id (origin_id),
    INDEX idx_destination_id (destination_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sender-Recipient ManyToMany 관계 (Letter 중간 테이블)
CREATE TABLE IF NOT EXISTS senders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recipients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS letters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sent_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    delivery_status VARCHAR(20) DEFAULT 'SENT',
    priority VARCHAR(10) DEFAULT 'NORMAL',
    FOREIGN KEY (sender_id) REFERENCES senders(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES recipients(id) ON DELETE CASCADE,
    INDEX idx_sender_id (sender_id),
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_sent_date (sent_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 상속 계층 구조 테이블들
-- ===========================================

-- Family 상속 계층 (Joined Strategy)
CREATE TABLE IF NOT EXISTS family (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    scientific_name VARCHAR(100) NOT NULL,
    habitat VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS canidae (
    id BIGINT PRIMARY KEY,
    pack_size INT DEFAULT 1,
    hunting_style VARCHAR(100),
    FOREIGN KEY (id) REFERENCES family(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS felidae (
    id BIGINT PRIMARY KEY,
    climbing_ability VARCHAR(50),
    hunting_style VARCHAR(100),
    FOREIGN KEY (id) REFERENCES family(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scincidae (
    id BIGINT PRIMARY KEY,
    scale_type VARCHAR(50),
    burrowing_ability VARCHAR(50),
    FOREIGN KEY (id) REFERENCES family(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Item 상속 계층 (Single Table Strategy)
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dtype VARCHAR(31) NOT NULL, -- ALBUM, BOOK, MOVIE
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock_quantity INT DEFAULT 0,
    -- Album 전용 필드
    artist VARCHAR(100),
    etc VARCHAR(255),
    -- Book 전용 필드
    author VARCHAR(100),
    isbn VARCHAR(20),
    -- Movie 전용 필드
    director VARCHAR(100),
    actor VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dtype (dtype)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Contact 상속 계층 (Table Per Class Strategy)
CREATE TABLE IF NOT EXISTS contact_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) DEFAULT 'South Korea',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contact_emails (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    domain VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 초기 테스트 데이터 삽입
-- ===========================================

-- OneToOne 관계 테스트 데이터
INSERT INTO members (name, email) VALUES 
('김철수', 'kim@primavera.com'),
('이영희', 'lee@primavera.com'),
('박민수', 'park@primavera.com');

INSERT INTO addresses (member_id, street, city, postal_code) VALUES 
(1, '강남대로 123', '서울특별시', '06123'),
(2, '해운대해변로 456', '부산광역시', '48094'),
(3, '중앙로 789', '대구광역시', '41911');

INSERT INTO products (name, description, price) VALUES 
('MacBook Pro', 'Apple 노트북 컴퓨터', 2500000.00),
('iPhone 15', 'Apple 스마트폰', 1200000.00),
('Galaxy S24', 'Samsung 스마트폰', 1100000.00);

INSERT INTO serials (product_id, serial_number, manufacture_date) VALUES 
(1, 'MBP2024001', '2024-01-15'),
(2, 'IPH2024001', '2024-02-20'),
(3, 'GAL2024001', '2024-03-10');

-- OneToMany 관계 테스트 데이터
INSERT INTO customers (name, email, phone, status) VALUES 
('고객1', 'customer1@test.com', '010-1111-1111', 'ACTIVE'),
('고객2', 'customer2@test.com', '010-2222-2222', 'ACTIVE');

INSERT INTO contacts (customer_id, contact_type, contact_value, is_primary) VALUES 
(1, 'EMAIL', 'customer1@test.com', TRUE),
(1, 'PHONE', '010-1111-1111', FALSE),
(1, 'ADDRESS', '서울시 강남구', FALSE),
(2, 'EMAIL', 'customer2@test.com', TRUE),
(2, 'PHONE', '010-2222-2222', FALSE);

INSERT INTO professors (name, department, email, office, hire_date) VALUES 
('김교수', '컴퓨터공학과', 'prof.kim@univ.ac.kr', '공학관 301', '2010-03-01'),
('이교수', '전자공학과', 'prof.lee@univ.ac.kr', '공학관 401', '2015-09-01');

INSERT INTO students (professor_id, student_number, name, major, grade, email, enrollment_date) VALUES 
(1, '2021001', '학생1', '컴퓨터공학', 3, 'student1@univ.ac.kr', '2021-03-01'),
(1, '2021002', '학생2', '컴퓨터공학', 2, 'student2@univ.ac.kr', '2022-03-01'),
(2, '2021003', '학생3', '전자공학', 4, 'student3@univ.ac.kr', '2020-03-01');

-- ManyToOne 관계 테스트 데이터
INSERT INTO departments (name, code, manager_name, budget, established_date) VALUES 
('개발팀', 'DEV', '개발팀장', 1000000000.00, '2020-01-01'),
('마케팅팀', 'MKT', '마케팅팀장', 500000000.00, '2020-01-01'),
('영업팀', 'SALES', '영업팀장', 800000000.00, '2020-01-01');

INSERT INTO employees (department_id, employee_number, name, position, salary, hire_date, email) VALUES 
(1, 'EMP001', '개발자1', 'Senior Developer', 8000000.00, '2022-01-01', 'dev1@company.com'),
(1, 'EMP002', '개발자2', 'Junior Developer', 5000000.00, '2023-01-01', 'dev2@company.com'),
(2, 'EMP003', '마케터1', 'Marketing Manager', 7000000.00, '2022-06-01', 'mkt1@company.com'),
(3, 'EMP004', '영업1', 'Sales Representative', 6000000.00, '2022-03-01', 'sales1@company.com');

INSERT INTO teams (name, city, founded_year, league, budget) VALUES 
('FC Seoul', '서울', 1983, 'K League 1', 15000000000.00),
('Busan IPark', '부산', 1979, 'K League 1', 8000000000.00);

INSERT INTO players (team_id, jersey_number, name, position, age, nationality, salary, contract_start_date, contract_end_date) VALUES 
(1, 10, '손흥민', 'Forward', 31, 'South Korea', 5000000000.00, '2024-01-01', '2026-12-31'),
(1, 7, '이강인', 'Midfielder', 23, 'South Korea', 2000000000.00, '2024-01-01', '2025-12-31'),
(2, 9, '황희찬', 'Forward', 28, 'South Korea', 3000000000.00, '2024-01-01', '2025-12-31');

-- ManyToMany 관계 테스트 데이터
INSERT INTO buyers (name, company, email, phone) VALUES 
('구매자1', '구매회사A', 'buyer1@companya.com', '02-1111-1111'),
('구매자2', '구매회사B', 'buyer2@companyb.com', '02-2222-2222');

INSERT INTO sellers (name, company, email, phone) VALUES 
('판매자1', '판매회사X', 'seller1@companyx.com', '02-3333-3333'),
('판매자2', '판매회사Y', 'seller2@companyy.com', '02-4444-4444');

INSERT INTO contracts (buyer_id, seller_id, contract_number, amount, contract_date, delivery_date, status) VALUES 
(1, 1, 'CON-2024-001', 10000000.00, '2024-01-15', '2024-02-15', 'COMPLETED'),
(1, 2, 'CON-2024-002', 15000000.00, '2024-02-01', '2024-03-01', 'IN_PROGRESS'),
(2, 1, 'CON-2024-003', 8000000.00, '2024-01-20', '2024-02-20', 'COMPLETED');

-- 상속 계층 테스트 데이터
INSERT INTO family (name, scientific_name, habitat) VALUES 
('개과', 'Canidae', '전 세계 다양한 서식지'),
('고양이과', 'Felidae', '숲, 초원, 사막 등'),
('도마뱀과', 'Scincidae', '땅속, 낙엽 아래');

INSERT INTO canidae (id, pack_size, hunting_style) VALUES 
(1, 8, '무리 사냥');

INSERT INTO felidae (id, climbing_ability, hunting_style) VALUES 
(2, '뛰어남', '단독 사냥');

INSERT INTO scincidae (id, scale_type, burrowing_ability) VALUES 
(3, '매끄러운 비늘', '뛰어남');

INSERT INTO items (dtype, name, price, stock_quantity, artist, author, director) VALUES 
('ALBUM', 'Love Yourself', 25000.00, 100, 'BTS', NULL, NULL),
('BOOK', 'Spring Boot 완전정복', 35000.00, 50, NULL, '김개발', NULL),
('MOVIE', 'Parasite', 15000.00, 200, NULL, NULL, '봉준호');

INSERT INTO contact_addresses (name, street, city, postal_code) VALUES 
('주소연락처1', '테헤란로 123', '서울', '06142'),
('주소연락처2', '해운대해변로 456', '부산', '48094');

INSERT INTO contact_emails (name, email, is_verified, domain) VALUES 
('이메일연락처1', 'contact1@test.com', TRUE, 'test.com'),
('이메일연락처2', 'contact2@example.com', FALSE, 'example.com');