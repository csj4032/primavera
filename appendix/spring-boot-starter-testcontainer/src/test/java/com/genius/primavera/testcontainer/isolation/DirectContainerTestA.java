package com.genius.primavera.testcontainer.isolation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 직접 컨테이너 관리 방식의 격리 테스트 A
 * @Container와 @DynamicPropertySource를 사용하여 독립적인 컨테이너 생성
 */
@Slf4j
@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("직접 컨테이너 관리 격리 테스트 A")
class DirectContainerTestA {

    @Container
    static MariaDBContainer<?> mariaDBContainer;
    
    static {
        mariaDBContainer = new MariaDBContainer<>("mariadb:11.4.7")
                .withUsername("primavera")
                .withPassword("primavera")
                .withDatabaseName("primavera")
                .withInitScript("sql/init.sql")
                .withReuse(false)
                .withLabel("test-class", "DirectContainerTestA");
        
        // 컨테이너를 즉시 시작
        mariaDBContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariaDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDBContainer::getUsername);
        registry.add("spring.datasource.password", mariaDBContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final AtomicInteger testCounter = new AtomicInteger(0);

    @BeforeAll
    void beforeAllTests() {
        log.info("=== DirectContainerTestA 시작 ===");
        log.info("테스트 A - MariaDB 컨테이너 URL: {}", mariaDBContainer.getJdbcUrl());
        log.info("테스트 A - MariaDB 호스트:포트: {}:{}", mariaDBContainer.getHost(), mariaDBContainer.getMappedPort(3306));
        log.info("테스트 A - 컨테이너 ID: {}", mariaDBContainer.getContainerId());
        log.info("테스트 A - 컨테이너 실행 상태: {}", mariaDBContainer.isRunning());
    }

    @BeforeEach
    void beforeEachTest() {
        int testNum = testCounter.incrementAndGet();
        log.info("테스트 A - {} 번째 테스트 시작", testNum);
        log.info("테스트 A - 현재 컨테이너 상태: running={}, host:port={}:{}", 
                 mariaDBContainer.isRunning(), mariaDBContainer.getHost(), mariaDBContainer.getMappedPort(3306));
    }

    @Test
    @Order(1)
    @DisplayName("직접 테스트 A-1: 고유 데이터 삽입 및 확인")
    void testA1_insertUniqueData() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 직접 테스트 A-1 시작 - 고유 데이터 삽입", threadName);
        
        Thread.sleep(200);
        
        // 초기 데이터 수 확인
        Integer initialCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("직접 테스트 A-1 - 초기 사용자 수: {}", initialCount);
        
        // 고유한 테스트 A 데이터 삽입
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO test_users (name, email) VALUES ('DirectTestA User %d', 'directA_%d@containerA.com')", 
                i, i
            ));
        }
        
        // 테스트 A 데이터만 조회
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        assertThat(testACount).isEqualTo(5);
        
        // 테스트 B 데이터가 없는지 확인 (격리 확인)
        Integer testBCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerB.com'", 
            Integer.class
        );
        assertThat(testBCount).isEqualTo(0);
        
        log.info("직접 테스트 A-1 완료 - TestA 데이터: {} 건, TestB 데이터: {} 건", testACount, testBCount);
    }

    @Test
    @Order(2)
    @DisplayName("직접 테스트 A-2: 컨테이너 격리 상태 지속 확인")
    void testA2_verifyContainerIsolation() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 직접 테스트 A-2 시작 - 격리 상태 지속 확인", threadName);
        
        Thread.sleep(150);
        
        // 이전 테스트의 데이터가 유지되는지 확인
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        assertThat(testACount).isEqualTo(5);
        
        // 추가 데이터 삽입
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('DirectTestA Additional', 'directA_additional@containerA.com')");
        
        // 전체 테스트 A 데이터 확인
        Integer totalTestACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        assertThat(totalTestACount).isEqualTo(6);
        
        // 여전히 테스트 B 데이터는 없어야 함
        Integer testBCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerB.com'", 
            Integer.class
        );
        assertThat(testBCount).isEqualTo(0);
        
        log.info("직접 테스트 A-2 완료 - 총 TestA 데이터: {} 건, TestB 데이터: {} 건", totalTestACount, testBCount);
    }

    @Test
    @Order(3)
    @DisplayName("직접 테스트 A-3: 최종 격리 상태 확인")
    void testA3_finalIsolationCheck() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 직접 테스트 A-3 시작 - 최종 격리 상태 확인", threadName);
        
        Thread.sleep(100);
        
        // 최종 데이터 상태 확인
        Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        Integer testAUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        
        log.info("직접 테스트 A-3 - 최종 상태: 전체 사용자 {} 명, TestA 사용자 {} 명", totalUsers, testAUsers);
        
        // 최종 마커 삽입
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('DIRECT_FINAL_TEST_A_MARKER', 'direct_final_marker@containerA.com')");
        
        Integer finalMarkerCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email = 'direct_final_marker@containerA.com'", 
            Integer.class
        );
        assertThat(finalMarkerCount).isEqualTo(1);
        
        log.info("직접 테스트 A-3 완료 - 최종 마커 삽입 완료");
    }

    @AfterAll
    void afterAllTests() {
        log.info("=== DirectContainerTestA 종료 ===");
        log.info("총 실행된 테스트 수: {}", testCounter.get());
        log.info("최종 컨테이너 정보:");
        log.info("  - URL: {}", mariaDBContainer.getJdbcUrl());
        log.info("  - Host:Port: {}:{}", mariaDBContainer.getHost(), mariaDBContainer.getMappedPort(3306));
        log.info("  - Container ID: {}", mariaDBContainer.getContainerId());
        log.info("  - Running: {}", mariaDBContainer.isRunning());
    }
}