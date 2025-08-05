package com.genius.primavera.testcontainer.containertype;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MySQL 컨테이너 타입 테스트
 */
@Slf4j
@SpringBootTest
@DisplayName("MySQL TestContainer 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableTestContainers(containers = ContainerType.MYSQL)
class MySQLContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("MySQL 컨테이너 연결 및 기본 쿼리 테스트")
    void testMySQLConnection() {
        log.info("MySQL 컨테이너 연결 테스트 시작");
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        log.info("MySQL 버전: {}", version);
        assertThat(version).contains("8.0");
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("초기 사용자 수: {}", userCount);
        assertThat(userCount).isEqualTo(2);
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('MySQL User', 'mysql@test.com')");
        Integer newCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        assertThat(newCount).isEqualTo(3);
        log.info("MySQL 컨테이너 테스트 완료");
    }

    @Test
    @Order(2)
    @DisplayName("MySQL 트랜잭션 테스트")
    void testMySQLTransaction() {
        log.info("MySQL 트랜잭션 테스트 시작");
        try {
            jdbcTemplate.execute("START TRANSACTION");
            jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('Transaction User 1', 'trans1@mysql.com')");
            jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('Transaction User 2', 'trans2@mysql.com')");
            Integer transCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email LIKE '%@mysql.com'", Integer.class);
            assertThat(transCount).isEqualTo(2);
            jdbcTemplate.execute("COMMIT");
        } catch (Exception e) {
            jdbcTemplate.execute("ROLLBACK");
            throw e;
        }
        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email LIKE '%@mysql.com'", Integer.class);
        assertThat(finalCount).isEqualTo(2);
        log.info("MySQL 트랜잭션 테스트 완료");
    }
}