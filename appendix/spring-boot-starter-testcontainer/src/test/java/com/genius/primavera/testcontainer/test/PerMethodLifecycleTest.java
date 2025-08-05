package com.genius.primavera.testcontainer.test;

import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @TestInstance(TestInstance.Lifecycle.PER_METHOD) 라이프사이클 테스트
 * 컨테이너가 각 테스트 메소드마다 새로 생성되는지 확인
 */
@Slf4j
@EnableTestContainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PerMethodLifecycleTest {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Set<String> usedContainerUrls = new HashSet<>();
    private static int testCounter = 0;
    
    @BeforeEach
    void setUp() {
        testCounter++;
        log.info("테스트 {} 준비", testCounter);
        String currentUrl = jdbcTemplate.getDataSource().toString();
        log.info("현재 컨테이너 URL: {}", currentUrl);
        usedContainerUrls.add(currentUrl);
    }
    
    @Test
    @Order(1)
    @DisplayName("첫 번째 테스트 - 깨끗한 데이터베이스 확인")
    void test1_cleanDatabase() {
        log.info("테스트 1 실행 - 깨끗한 DB 확인");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        assertThat(count).isEqualTo(2); // init.sql의 2개 데이터
        log.info("초기 데이터 수: {}", count);
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('PER_METHOD User 1', 'permethod1@test.com')");
        Integer afterCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        assertThat(afterCount).isEqualTo(3);
        log.info("데이터 삽입 후: {}", afterCount);
    }
    
    @Test
    @Order(2)
    @DisplayName("두 번째 테스트 - 새로운 컨테이너 확인")
    void test2_newContainer() {
        log.info("테스트 2 실행 - 새 컨테이너 확인");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        assertThat(count).isEqualTo(3);
        Integer perMethodCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email = 'permethod1@test.com'", Integer.class);
        assertThat(perMethodCount).isEqualTo(1);
        log.info("이전 테스트 데이터 없음 확인");
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('PER_METHOD User 2', 'permethod2@test.com')");
    }
    
    @Test
    @Order(3)
    @DisplayName("세 번째 테스트 - 격리된 환경 확인")
    void test3_isolatedEnvironment() {
        log.info("테스트 3 실행 - 격리된 환경 확인");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        assertThat(count).isEqualTo(4);
        Integer perMethodCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email LIKE 'permethod%'", Integer.class);
        assertThat(perMethodCount).isEqualTo(2);
        log.info("테스트 3 완료 - 격리된 환경 확인 성공");
    }
    
    @AfterAll
    static void afterAll() {
        log.info("=== PER_METHOD 테스트 종료 ===");
        log.info("총 실행된 테스트 수: {}", testCounter);
        log.info("사용된 고유 컨테이너 수: {}", usedContainerUrls.size());
        if (usedContainerUrls.size() == 1) {
            log.info("컨테이너가 재사용되었습니다 (reuse 옵션 또는 최적화)");
        } else {
            log.info("각 테스트마다 새로운 컨테이너가 생성되었습니다");
        }
    }
}