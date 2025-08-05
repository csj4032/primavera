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
 * 직접 컨테이너 관리 방식의 격리 테스트 B
 * @Container와 @DynamicPropertySource를 사용하여 독립적인 컨테이너 생성
 */
@Slf4j
@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("직접 컨테이너 관리 격리 테스트 B")
class DirectContainerTestB {

    @Container
    static MariaDBContainer<?> mariaDBContainer;
    
    static {
        mariaDBContainer = new MariaDBContainer<>("mariadb:11.4.7")
                .withUsername("primavera")
                .withPassword("primavera")
                .withDatabaseName("primavera")
                .withInitScript("sql/init.sql")
                .withReuse(false)
                .withLabel("test-class", "DirectContainerTestB");
        
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
        log.info("=== DirectContainerTestB 시작 ===");
        log.info("테스트 B - MariaDB 컨테이너 URL: {}", mariaDBContainer.getJdbcUrl());
        log.info("테스트 B - MariaDB 호스트:포트: {}:{}", mariaDBContainer.getHost(), mariaDBContainer.getMappedPort(3306));
        log.info("테스트 B - 컨테이너 ID: {}", mariaDBContainer.getContainerId());
        log.info("테스트 B - 컨테이너 실행 상태: {}", mariaDBContainer.isRunning());
    }

    @BeforeEach
    void beforeEachTest() {
        int testNum = testCounter.incrementAndGet();
        log.info("테스트 B - {} 번째 테스트 시작", testNum);
        log.info("테스트 B - 현재 컨테이너 상태: running={}, host:port={}:{}", 
                 mariaDBContainer.isRunning(), mariaDBContainer.getHost(), mariaDBContainer.getMappedPort(3306));
    }

    @Test
    @Order(1)
    @DisplayName("직접 테스트 B-1: 고유 데이터 삽입 및 확인")
    void testB1_insertUniqueData() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 직접 테스트 B-1 시작 - 고유 데이터 삽입", threadName);
        
        Thread.sleep(180);
        
        // 초기 데이터 수 확인
        Integer initialCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("직접 테스트 B-1 - 초기 사용자 수: {}", initialCount);
        
        // 고유한 테스트 B 데이터 삽입
        for (int i = 1; i <= 7; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO test_users (name, email) VALUES ('DirectTestB User %d', 'directB_%d@containerB.com')", 
                i, i
            ));
        }
        
        // 테스트 B 데이터만 조회
        Integer testBCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerB.com'", 
            Integer.class
        );
        assertThat(testBCount).isEqualTo(7);
        
        // 테스트 A 데이터가 없는지 확인 (격리 확인)
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        assertThat(testACount).isEqualTo(0);
        
        log.info("직접 테스트 B-1 완료 - TestB 데이터: {} 건, TestA 데이터: {} 건", testBCount, testACount);
    }

    @Test
    @Order(2)
    @DisplayName("직접 테스트 B-2: 대량 데이터 처리")
    void testB2_bulkDataProcessing() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 직접 테스트 B-2 시작 - 대량 데이터 처리", threadName);
        
        Thread.sleep(220);
        
        // 대량 데이터 삽입
        long startTime = System.currentTimeMillis();
        for (int i = 1; i <= 30; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO test_users (name, email) VALUES ('DirectTestB Bulk %d', 'directB_bulk_%d@containerB.com')", 
                i, i
            ));
        }
        long insertTime = System.currentTimeMillis() - startTime;
        
        // 전체 테스트 B 데이터 확인
        Integer totalTestBCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerB.com'", 
            Integer.class
        );
        assertThat(totalTestBCount).isEqualTo(37); // 7 + 30
        
        // 여전히 테스트 A 데이터는 없어야 함
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        assertThat(testACount).isEqualTo(0);
        
        log.info("직접 테스트 B-2 완료 - 대량 삽입 시간: {}ms, 총 TestB 데이터: {} 건", insertTime, totalTestBCount);
    }

    @Test
    @Order(3)
    @DisplayName("직접 테스트 B-3: 복합 쿼리 및 통계")
    void testB3_complexQueryAndStats() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 직접 테스트 B-3 시작 - 복합 쿼리 및 통계", threadName);
        
        Thread.sleep(100);
        
        // 복잡한 통계 쿼리
        var stats = jdbcTemplate.queryForMap("""
            SELECT 
                COUNT(*) as total_count,
                COUNT(DISTINCT SUBSTRING_INDEX(email, '@', -1)) as unique_domains,
                AVG(LENGTH(name)) as avg_name_length
            FROM test_users 
            WHERE email LIKE '%@containerB.com'
        """);
        
        assertThat(((Number) stats.get("total_count")).intValue()).isEqualTo(37);
        assertThat(((Number) stats.get("unique_domains")).intValue()).isEqualTo(1); // containerB.com만
        
        log.info("직접 테스트 B-3 - 통계 결과: {}", stats);
        
        // 격리 상태 재확인
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        assertThat(testACount).isEqualTo(0);
        
        log.info("직접 테스트 B-3 완료 - 격리 상태 유지 확인");
    }

    @Test
    @Order(4)
    @DisplayName("직접 테스트 B-4: 최종 상태 확인")
    void testB4_finalStateVerification() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 직접 테스트 B-4 시작 - 최종 상태 확인", threadName);
        
        Thread.sleep(80);
        
        // 최종 데이터 상태 확인
        Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        Integer testBUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerB.com'", 
            Integer.class
        );
        
        log.info("직접 테스트 B-4 - 최종 상태: 전체 사용자 {} 명, TestB 사용자 {} 명", totalUsers, testBUsers);
        
        // 최종 마커 삽입
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('DIRECT_FINAL_TEST_B_MARKER', 'direct_final_marker@containerB.com')");
        
        Integer finalMarkerCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email = 'direct_final_marker@containerB.com'", 
            Integer.class
        );
        assertThat(finalMarkerCount).isEqualTo(1);
        
        log.info("직접 테스트 B-4 완료 - 최종 마커 삽입 완료");
    }

    @AfterAll
    void afterAllTests() {
        log.info("=== DirectContainerTestB 종료 ===");
        log.info("총 실행된 테스트 수: {}", testCounter.get());
        log.info("최종 컨테이너 정보:");
        log.info("  - URL: {}", mariaDBContainer.getJdbcUrl());
        log.info("  - Host:Port: {}:{}", mariaDBContainer.getHost(), mariaDBContainer.getMappedPort(3306));
        log.info("  - Container ID: {}", mariaDBContainer.getContainerId());
        log.info("  - Running: {}", mariaDBContainer.isRunning());
    }
}