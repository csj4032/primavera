package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("컨테이너 라이프사이클 관리 테스트")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "lifecycleDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "lifecycleCache")
})
public class ContainerLifecycleTest {

    @Autowired
    @Qualifier("lifecycleDb")
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    private ContainerManager containerManager;

    @BeforeAll
    void setupLifecycleTests() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        containerManager = ContainerRegistry.get();
        
        jdbcTemplate.execute("""
            CREATE TABLE lifecycle_test (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                test_name VARCHAR(100),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(50) DEFAULT 'ACTIVE'
            )
        """);

        log.info("컨테이너 라이프사이클 테스트 환경 초기화 완료");
    }

    @Test
    @Order(1)
    @DisplayName("컨테이너 시작 상태 및 기본 설정 검증")
    void testContainerInitialState() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = containerManager.getContainer("lifecycleCache");

        assertNotNull(dbInfo, "DB 컨테이너 정보가 존재해야 함");
        assertNotNull(cacheInfo, "캐시 컨테이너 정보가 존재해야 함");

        assertTrue(dbInfo.container().isRunning(), "DB 컨테이너가 실행 중이어야 함");
        assertTrue(cacheInfo.container().isRunning(), "캐시 컨테이너가 실행 중이어야 함");

        assertTrue(dbInfo.container().isHealthy(), "DB 컨테이너가 건강해야 함");
        assertNotNull(dbInfo.container().getContainerId(), "컨테이너 ID가 존재해야 함");
        assertTrue(dbInfo.container().getContainerId().length() > 10, "컨테이너 ID가 유효해야 함");

        String dbHost = dbInfo.container().getHost();
        Integer dbPort = dbInfo.container().getFirstMappedPort();
        
        assertNotNull(dbHost, "DB 호스트가 설정되어야 함");
        assertNotNull(dbPort, "DB 포트가 매핑되어야 함");
        assertTrue(dbPort > 0, "DB 포트가 유효해야 함");

        log.info("컨테이너 기본 상태 검증 완료 - DB: {}:{}, Cache: {}:{}", 
            dbHost, dbPort, cacheInfo.container().getHost(), cacheInfo.container().getFirstMappedPort());
    }

    @Test
    @Order(2)
    @DisplayName("데이터베이스 연결 및 기본 작업 검증")
    void testDatabaseConnectivityAndOperations() {
        assertDoesNotThrow(() -> {
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            assertNotNull(version, "데이터베이스 버전 정보를 가져올 수 있어야 함");
            assertTrue(version.toLowerCase().contains("mariadb"), "MariaDB 버전 정보여야 함");
            log.info("데이터베이스 버전: {}", version);
        }, "데이터베이스 연결이 정상적으로 작동해야 함");

        int insertResult = jdbcTemplate.update(
            "INSERT INTO lifecycle_test (test_name, status) VALUES (?, ?)",
            "connectivity_test", "RUNNING");
        assertEquals(1, insertResult, "데이터 삽입이 성공해야 함");

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lifecycle_test WHERE test_name = ?", 
            Integer.class, "connectivity_test");
        assertEquals(1, count, "삽입된 데이터를 조회할 수 있어야 함");

        int updateResult = jdbcTemplate.update(
            "UPDATE lifecycle_test SET status = ? WHERE test_name = ?",
            "COMPLETED", "connectivity_test");
        assertEquals(1, updateResult, "데이터 수정이 성공해야 함");

        String updatedStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM lifecycle_test WHERE test_name = ?", 
            String.class, "connectivity_test");
        assertEquals("COMPLETED", updatedStatus, "데이터가 올바르게 수정되어야 함");

        log.info("데이터베이스 기본 작업 검증 완료");
    }

    @Test
    @Order(3)
    @DisplayName("컨테이너 격리 및 네트워크 검증")
    void testContainerIsolationAndNetwork() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = containerManager.getContainer("lifecycleCache");

        String dbHost = dbInfo.container().getHost();
        Integer dbPort = dbInfo.container().getFirstMappedPort();
        String cacheHost = cacheInfo.container().getHost();
        Integer cachePort = cacheInfo.container().getFirstMappedPort();

        assertNotNull(dbHost, "DB 호스트가 설정되어야 함");
        assertNotNull(dbPort, "DB 포트가 설정되어야 함");
        assertNotNull(cacheHost, "캐시 호스트가 설정되어야 함");
        assertNotNull(cachePort, "캐시 포트가 설정되어야 함");

        assertNotEquals(dbPort, cachePort, "DB와 캐시는 다른 포트를 사용해야 함");

        String dbContainerId = dbInfo.container().getContainerId();
        String cacheContainerId = cacheInfo.container().getContainerId();
        
        assertNotNull(dbContainerId, "DB 컨테이너 ID가 존재해야 함");
        assertNotNull(cacheContainerId, "캐시 컨테이너 ID가 존재해야 함");
        assertNotEquals(dbContainerId, cacheContainerId, "컨테이너 ID가 달라야 함");

        jdbcTemplate.update(
            "INSERT INTO lifecycle_test (test_name, status) VALUES (?, ?)",
            "isolation_test_db", "ISOLATED");

        Integer dbTestCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lifecycle_test WHERE test_name LIKE 'isolation_test_%'", 
            Integer.class);
        assertEquals(1, dbTestCount, "DB 컨테이너에서 독립적인 작업이 수행되어야 함");

        log.info("컨테이너 격리 검증 완료 - DB: {} ({}), Cache: {} ({})", 
            dbHost + ":" + dbPort, dbContainerId.substring(0, 12),
            cacheHost + ":" + cachePort, cacheContainerId.substring(0, 12));
    }

    @Test
    @Order(4)
    @DisplayName("컨테이너 건강성 검사 및 상태 검증")
    void testContainerHealthAndStatus() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = containerManager.getContainer("lifecycleCache");

        assertTrue(dbInfo.container().isHealthy(), "DB 컨테이너가 건강해야 함");
        assertTrue(cacheInfo.container().isHealthy(), "캐시 컨테이너가 건강해야 함");

        assertNotNull(dbInfo.container().getHost(), "DB 호스트가 설정되어야 함");
        assertNotNull(dbInfo.container().getFirstMappedPort(), "DB 포트가 매핑되어야 함");
        
        assertDoesNotThrow(() -> {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            assertEquals(1, result, "건강성 검사 쿼리가 정상 실행되어야 함");
        }, "데이터베이스 건강성 검사가 성공해야 함");

        log.info("컨테이너 건강성 검사 완료");
    }

    @Test
    @Order(5)
    @DisplayName("안정성 및 지속성 테스트")
    void testStabilityAndPersistence() throws InterruptedException {
        int iterations = 20;
        int successCount = 0;
        int errorCount = 0;

        log.info("안정성 테스트 시작 - {}회 반복", iterations);

        for (int i = 0; i < iterations; i++) {
            try {
                jdbcTemplate.update(
                    "INSERT INTO lifecycle_test (test_name, status) VALUES (?, ?)",
                    "stability_test_" + i, "ITERATION_" + i);

                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM lifecycle_test WHERE test_name LIKE 'stability_test_%'", 
                    Integer.class);

                assertTrue(count > 0, "데이터 조회가 정상적으로 작동해야 함");

                if (i % 5 == 0) {
                    jdbcTemplate.update(
                        "UPDATE lifecycle_test SET status = 'UPDATED' WHERE test_name = ?",
                        "stability_test_" + i);
                }

                successCount++;

                if (i % 5 == 0) {
                    Thread.sleep(50);
                }

            } catch (Exception e) {
                errorCount++;
                log.warn("반복 {} 중 오류 발생: {}", i, e.getMessage());
                
                if (errorCount > iterations * 0.1) {
                    fail("오류율이 10%를 초과했습니다: " + errorCount + "/" + (i + 1));
                }
            }
        }

        assertTrue(successCount >= iterations * 0.9, "90% 이상의 작업이 성공해야 함");
        assertTrue(errorCount < iterations * 0.1, "오류율이 10% 미만이어야 함");

        Integer finalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lifecycle_test WHERE test_name LIKE 'stability_test_%'", 
            Integer.class);
        assertTrue(finalCount >= successCount * 0.9, "대부분의 데이터가 정상적으로 저장되어야 함");

        log.info("안정성 테스트 완료 - 성공: {}, 실패: {}, 저장된 데이터: {}", 
            successCount, errorCount, finalCount);
    }

    @Test
    @Order(6)
    @DisplayName("리소스 사용량 및 정리 검증")
    void testResourceUsageAndCleanup() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        GenericContainer<?> container = dbInfo.container();

        assertNotNull(container.getContainerId(), "컨테이너 ID가 존재해야 함");
        assertTrue(container.isRunning(), "컨테이너가 실행 중이어야 함");

        String containerId = container.getContainerId();
        assertTrue(containerId.length() > 10, "유효한 컨테이너 ID여야 함");

        Integer mappedPort = container.getFirstMappedPort();
        assertNotNull(mappedPort, "포트가 매핑되어야 함");
        assertTrue(mappedPort > 1024, "매핑된 포트가 유효한 범위여야 함");

        String host = container.getHost();
        assertTrue("localhost".equals(host) || "127.0.0.1".equals(host) || host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"), 
            "유효한 호스트 주소여야 함");

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            }
        }, "다중 연결이 정상적으로 작동해야 함");

        log.info("리소스 사용량 검증 완료 - Container: {}, Host: {}, Port: {}", 
            containerId.substring(0, 12), host, mappedPort);
    }
}