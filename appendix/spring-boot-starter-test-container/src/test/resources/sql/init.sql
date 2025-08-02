CREATE TABLE IF NOT EXISTS test_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO test_table (name) VALUES ('Test Data 1');
INSERT INTO test_table (name) VALUES ('Test Data 2');