CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255),
    password VARCHAR(255),
    nickname VARCHAR(255),
    role VARCHAR(10),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
