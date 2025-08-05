package com.genius.primavera.testcontainer.test;

import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @TestInstance(TestInstance.Lifecycle.PER_CLASS) 라이프사이클 테스트
 * 컨테이너가 클래스 레벨에서 한 번만 생성되고 모든 테스트에서 공유되는지 확인
 */
@Slf4j
@EnableTestContainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PerClassLifecycleTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static String containerJdbcUrl;
    private int testCounter = 0;

    @BeforeAll
    void beforeAll() {
        log.info("=== PER_CLASS 테스트 시작 ===");
    }

    @BeforeEach
    void setUp() {
        testCounter++;

        // 현재 JDBC URL 저장
        String currentUrl = jdbcTemplate.getDataSource().toString();
        if (containerJdbcUrl == null) {
            containerJdbcUrl = currentUrl;
            log.info("컨테이너 JDBC URL 저장: {}", containerJdbcUrl);
        }
    }

    @Test
    @Order(1)
    @DisplayName("첫 번째 테스트 - 데이터 삽입")
    void test1_insertData() {
        log.info("테스트 1 실행 - 데이터 삽입");

        // 데이터 삽입
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('PER_CLASS User', 'perclass@test.com')");

        // 삽입 확인
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_users WHERE email = 'perclass@test.com'",
                Integer.class
        );
        assertThat(count).isEqualTo(1);

        // 전체 데이터 수 확인
        Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("테스트 1 완료 - 전체 데이터 수: {}", totalCount);
    }

    @Test
    @Order(2)
    @DisplayName("두 번째 테스트 - 이전 테스트 데이터 확인")
    void test2_checkPreviousData() {
        log.info("테스트 2 실행 - 이전 데이터 확인");

        // 컨테이너가 동일한지 확인
        String currentUrl = jdbcTemplate.getDataSource().toString();
        assertThat(currentUrl).isEqualTo(containerJdbcUrl);
        log.info("동일한 컨테이너 사용 확인");

        // 이전 테스트에서 삽입한 데이터가 있는지 확인
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_users WHERE email = 'perclass@test.com'",
                Integer.class
        );
        assertThat(count).isEqualTo(1);
        log.info("이전 테스트 데이터 존재 확인");

        // 추가 데이터 삽입
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('PER_CLASS User 2', 'perclass2@test.com')");

        // 전체 데이터 수 확인
        Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("테스트 2 완료 - 전체 데이터 수: {}", totalCount);
    }

    @Test
    @Order(3)
    @DisplayName("세 번째 테스트 - 누적된 데이터 확인")
    void test3_checkAccumulatedData() {
        log.info("테스트 3 실행 - 누적 데이터 확인");

        // 컨테이너가 여전히 동일한지 확인
        String currentUrl = jdbcTemplate.getDataSource().toString();
        assertThat(currentUrl).isEqualTo(containerJdbcUrl);

        // 모든 테스트 데이터가 누적되어 있는지 확인
        Integer perclassCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_users WHERE email LIKE 'perclass%'",
                Integer.class
        );
        assertThat(perclassCount).isEqualTo(2);

        // 전체 데이터 수 확인 (init.sql의 2개 + 테스트에서 추가한 2개)
        Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        assertThat(totalCount).isGreaterThanOrEqualTo(4);
        log.info("테스트 3 완료 - 전체 데이터 수: {}", totalCount);
    }

    @AfterAll
    void afterAll() {
        log.info("=== PER_CLASS 테스트 종료 ===");
        log.info("총 실행된 테스트 수: {}", testCounter);
    }
}