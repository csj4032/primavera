package com.genius.primavera.testcontainer.containertype;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 컨테이너 타입 전환 테스트
 * 같은 테스트 클래스에서 어노테이션만 변경하여 다른 DB 엔진 테스트
 */
@Slf4j
class ContainerTypeSwitchingTest {

    /**
     * MariaDB 컨테이너 테스트
     */
    @SpringBootTest
    @EnableTestContainers(containers = ContainerType.MARIADB)
    @DisplayName("MariaDB 컨테이너 테스트")
    static class MariaDBTest {
        
        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        @DisplayName("MariaDB 특화 기능 테스트")
        void testMariaDBSpecificFeatures() {
            log.info("MariaDB 특화 기능 테스트 시작");
            
            // MariaDB 버전 확인
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            log.info("MariaDB 버전: {}", version);
            assertThat(version).containsIgnoringCase("MariaDB");
            
            // MariaDB JSON 함수 테스트
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS test_json_mariadb (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    data JSON
                )
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO test_json_mariadb (data) VALUES 
                ('{"database": "MariaDB", "version": 11.4, "features": ["JSON", "CTE", "Window Functions"]}')
            """);
            
            String dbName = jdbcTemplate.queryForObject(
                "SELECT JSON_EXTRACT(data, '$.database') FROM test_json_mariadb WHERE id = 1", 
                String.class
            );
            assertThat(dbName.replace("\"", "")).isEqualTo("MariaDB");
            
            log.info("MariaDB 특화 기능 테스트 완료");
        }

        @Test
        @DisplayName("MariaDB 윈도우 함수 테스트")
        void testMariaDBWindowFunctions() {
            log.info("MariaDB 윈도우 함수 테스트 시작");
            
            // 테스트 데이터 삽입
            for (int i = 1; i <= 10; i++) {
                jdbcTemplate.execute(String.format(
                    "INSERT INTO test_users (name, email) VALUES ('MariaDB User %d', 'user%d@mariadb.com')", 
                    i, i
                ));
            }
            
            // 윈도우 함수로 순위 매기기
            List<Map<String, Object>> results = jdbcTemplate.queryForList("""
                SELECT 
                    name,
                    email,
                    ROW_NUMBER() OVER (ORDER BY created_at) as row_num,
                    RANK() OVER (ORDER BY LENGTH(name)) as name_length_rank
                FROM test_users 
                WHERE email LIKE '%@mariadb.com'
                ORDER BY created_at
                LIMIT 5
            """);
            
            assertThat(results).hasSize(5);
            assertThat(results.get(0).get("row_num")).isNotNull();
            
            log.info("MariaDB 윈도우 함수 테스트 완료");
        }
    }

    /**
     * MySQL 컨테이너 테스트 (같은 SQL이지만 다른 엔진)
     */
    @SpringBootTest
    @EnableTestContainers(containers = ContainerType.MYSQL)
    @DisplayName("MySQL 컨테이너 테스트")
    static class MySQLTest {
        
        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        @DisplayName("MySQL 특화 기능 테스트")
        void testMySQLSpecificFeatures() {
            log.info("MySQL 특화 기능 테스트 시작");
            
            // MySQL 버전 확인
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            log.info("MySQL 버전: {}", version);
            assertThat(version).contains("8.0");
            
            // MySQL JSON 함수 테스트
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS test_json_mysql (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    data JSON
                )
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO test_json_mysql (data) VALUES 
                ('{"database": "MySQL", "version": 8.0, "features": ["JSON", "CTE", "Window Functions"]}')
            """);
            
            String dbName = jdbcTemplate.queryForObject(
                "SELECT JSON_EXTRACT(data, '$.database') FROM test_json_mysql WHERE id = 1", 
                String.class
            );
            assertThat(dbName.replace("\"", "")).isEqualTo("MySQL");
            
            log.info("MySQL 특화 기능 테스트 완료");
        }

        @Test
        @DisplayName("MySQL CTE 테스트")
        void testMySQLCTE() {
            log.info("MySQL CTE 테스트 시작");
            
            // MySQL 8.0의 CTE 기능 테스트
            List<Map<String, Object>> results = jdbcTemplate.queryForList("""
                WITH RECURSIVE fibonacci(n, fib_n, next_fib_n) AS (
                    SELECT 1, 0, 1
                    UNION ALL
                    SELECT n + 1, next_fib_n, fib_n + next_fib_n 
                    FROM fibonacci 
                    WHERE n < 10
                )
                SELECT n, fib_n FROM fibonacci
            """);
            
            assertThat(results).hasSize(10);
            assertThat(results.get(9).get("fib_n")).isEqualTo(34L); // 10번째 피보나치 수
            
            log.info("MySQL CTE 테스트 완료");
        }
    }

    /**
     * PostgreSQL 컨테이너 테스트 (같은 비즈니스 로직, 다른 SQL 방언)
     */
    @SpringBootTest
    @EnableTestContainers(containers = ContainerType.POSTGRESQL)
    @DisplayName("PostgreSQL 컨테이너 테스트")
    static class PostgreSQLTest {
        
        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        @DisplayName("PostgreSQL 특화 기능 테스트")
        void testPostgreSQLSpecificFeatures() {
            log.info("PostgreSQL 특화 기능 테스트 시작");
            
            // PostgreSQL 버전 확인
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            log.info("PostgreSQL 버전: {}", version);
            assertThat(version).containsIgnoringCase("PostgreSQL");
            
            // PostgreSQL JSONB 테스트
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS test_jsonb_postgres (
                    id SERIAL PRIMARY KEY,
                    data JSONB
                )
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO test_jsonb_postgres (data) VALUES 
                ('{"database": "PostgreSQL", "version": 15, "features": ["JSONB", "Arrays", "Custom Types"]}')
            """);
            
            String dbName = jdbcTemplate.queryForObject(
                "SELECT data->>'database' FROM test_jsonb_postgres WHERE id = 1", 
                String.class
            );
            assertThat(dbName).isEqualTo("PostgreSQL");
            
            // PostgreSQL 배열 타입 테스트
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS test_arrays (
                    id SERIAL PRIMARY KEY,
                    tags TEXT[],
                    numbers INTEGER[]
                )
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO test_arrays (tags, numbers) VALUES 
                ('{"postgresql", "database", "sql"}', '{1, 2, 3, 4, 5}')
            """);
            
            String firstTag = jdbcTemplate.queryForObject(
                "SELECT tags[1] FROM test_arrays WHERE id = 1", 
                String.class
            );
            assertThat(firstTag).isEqualTo("postgresql");
            
            log.info("PostgreSQL 특화 기능 테스트 완료");
        }

        @Test
        @DisplayName("PostgreSQL 고급 집계 함수 테스트")
        void testPostgreSQLAdvancedAggregates() {
            log.info("PostgreSQL 고급 집계 함수 테스트 시작");
            
            // 테스트 데이터 삽입
            for (int i = 1; i <= 10; i++) {
                jdbcTemplate.execute(String.format(
                    "INSERT INTO test_users (name, email) VALUES ('Postgres User %d', 'user%d@postgres.com')", 
                    i, i
                ));
            }
            
            // PostgreSQL의 고급 집계 함수 사용
            List<Map<String, Object>> results = jdbcTemplate.queryForList("""
                SELECT 
                    COUNT(*) as total_count,
                    ARRAY_AGG(name ORDER BY created_at) as all_names,
                    STRING_AGG(email, '; ' ORDER BY created_at) as all_emails
                FROM test_users 
                WHERE email LIKE '%@postgres.com'
            """);
            
            assertThat(results).hasSize(1);
            assertThat(results.get(0).get("total_count")).isEqualTo(10L);
            
            log.info("PostgreSQL 고급 집계 함수 테스트 완료");
        }
    }
}