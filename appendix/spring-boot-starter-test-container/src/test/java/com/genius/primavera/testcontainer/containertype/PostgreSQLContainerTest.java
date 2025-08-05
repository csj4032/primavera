package com.genius.primavera.testcontainer.containertype;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostgreSQL 컨테이너 타입 테스트
 */
@Slf4j
@SpringBootTest
@DisplayName("PostgreSQL TestContainer 테스트")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableTestContainers(containers = ContainerType.POSTGRESQL)
class PostgreSQLContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("PostgreSQL 컨테이너 연결 및 기본 쿼리 테스트")
    void testPostgreSQLConnection() {
        log.info("PostgreSQL 컨테이너 연결 테스트 시작");

        // 데이터베이스 버전 확인
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        log.info("PostgreSQL 버전: {}", version);
        assertThat(version).containsIgnoringCase("PostgreSQL");

        // 기본 데이터 확인 (데이터베이스에 이미 있는 데이터 수 확인)
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("초기 사용자 수: {}", userCount);
        assertThat(userCount).isGreaterThanOrEqualTo(2);  // 최초 2개 이상 있어야 함

        // 데이터 삽입 테스트 (유니크 이메일 사용)
        String uniqueEmail = "postgres-" + System.currentTimeMillis() + "@test.com";
        jdbcTemplate.execute(String.format(
            "INSERT INTO test_users (name, email) VALUES ('PostgreSQL User', '%s')", 
            uniqueEmail
        ));

        Integer newCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        assertThat(newCount).isGreaterThanOrEqualTo(3);

        log.info("PostgreSQL 컨테이너 테스트 완료");
    }

    @Test
    @DisplayName("PostgreSQL 고급 쿼리 테스트")
    void testPostgreSQLAdvancedQueries() {
        log.info("PostgreSQL 고급 쿼리 테스트 시작");

        // 고유한 이메일 접두어 생성
        String emailPrefix = "user-" + System.currentTimeMillis();
        
        // 대량 데이터 삽입
        for (int i = 1; i <= 10; i++) {
            jdbcTemplate.execute(String.format(
                    "INSERT INTO test_users (name, email) VALUES ('Postgres User %d', '%s-%d@postgres.com')",
                    i, emailPrefix, i
            ));
        }

        // 윈도우 함수 테스트 (PostgreSQL 특화 기능)
        List<Map<String, Object>> results = jdbcTemplate.queryForList(String.format("""
                    SELECT 
                        name,
                        email,
                        ROW_NUMBER() OVER (ORDER BY created_at) as row_num,
                        COUNT(*) OVER() as total_count
                    FROM test_users 
                    WHERE email LIKE '%s-%%@postgres.com'
                    ORDER BY created_at
                    LIMIT 5
                """, emailPrefix));

        assertThat(results).hasSize(5);
        assertThat(results.get(0).get("total_count")).isEqualTo(10L);

        // JSON 타입 테스트 (PostgreSQL 특화 기능)
        jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS test_json (
                        id SERIAL PRIMARY KEY,
                        data JSONB
                    )
                """);

        jdbcTemplate.execute("""
                    INSERT INTO test_json (data) VALUES 
                    ('{"name": "PostgreSQL", "version": 15, "features": ["JSONB", "Window Functions"]}')
                """);

        String jsonData = jdbcTemplate.queryForObject(
                "SELECT data->>'name' FROM test_json WHERE id = 1",
                String.class
        );
        assertThat(jsonData).isEqualTo("PostgreSQL");

        log.info("PostgreSQL 고급 쿼리 테스트 완료");
    }

    @Test
    @DisplayName("PostgreSQL CTE (Common Table Expression) 테스트")
    void testPostgreSQLCTE() {
        log.info("PostgreSQL CTE 테스트 시작");

        // CTE를 사용한 재귀 쿼리 테스트
        List<Map<String, Object>> results = jdbcTemplate.queryForList("""
                    WITH RECURSIVE numbers AS (
                        SELECT 1 as n
                        UNION ALL
                        SELECT n + 1 FROM numbers WHERE n < 5
                    )
                    SELECT n FROM numbers
                """);

        assertThat(results).hasSize(5);
        assertThat(results.get(4).get("n")).isEqualTo(5);

        log.info("PostgreSQL CTE 테스트 완료");
    }
}