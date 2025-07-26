-- Initial database setup for Primavera project with MariaDB 11.4.7
-- This file is executed when the MariaDB container starts for the first time

-- Set the character set and collation
ALTER DATABASE primavera CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create additional databases if needed for different chapters
CREATE DATABASE IF NOT EXISTS primavera_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges to primavera user for both databases
GRANT ALL PRIVILEGES ON primavera.* TO 'primavera'@'%';
GRANT ALL PRIVILEGES ON primavera_test.* TO 'primavera'@'%';

FLUSH PRIVILEGES;