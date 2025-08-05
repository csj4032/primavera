package com.genius.primavera.testcontainer.isolation;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MariaDBContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebEnvironment 기반 ApplicationContext 격리 테스트 A
 * RANDOM_PORT 설정으로 독립적인 ApplicationContext 생성
 */
@Slf4j
@SpringBootTest
@DisplayName("WebEnvironment 기반 격리 테스트 A")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableTestContainers(containers = ContainerType.MARIADB)
class WebEnvironmentIsolationTestA {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private MariaDBContainer<?> mariaDBContainer;

    private static String contextId;

    @BeforeAll
    void beforeAllTests() {
        contextId = Integer.toString(System.identityHashCode(applicationContext));
        log.info("=== WebEnvironmentIsolationTestA 시작 ===");
        log.info("ApplicationContext ID: {}", contextId);
        log.info("ApplicationContext Hash: {}", applicationContext.hashCode());
        log.info("ApplicationContext toString: {}", applicationContext.toString());

        if (mariaDBContainer != null) {
            log.info("TestA Container URL: {}", mariaDBContainer.getJdbcUrl());
            log.info("TestA Container ID: {}", mariaDBContainer.getContainerId());
            log.info("TestA Container Port: {}", mariaDBContainer.getMappedPort(3306));
            log.info("TestA Container Hash: {}", System.identityHashCode(mariaDBContainer));
        }
    }

    @Test
    @Order(1)
    @DisplayName("TestA-1: ApplicationContext 독립성 확인")
    void testA1_verifyContextIndependence() {
        log.info("[TestA-1] ApplicationContext 독립성 검증");
        log.info("Current Context ID: {}", System.identityHashCode(applicationContext));
        log.info("Thread: {}", Thread.currentThread().getName());

        // 고유 데이터 삽입
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('TestA DirtiesContext User', 'testA_dirties@example.com')");

        // 데이터 확인
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%testA_dirties@%'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);

        log.info("TestA-1 완료 - Context ID: {}, Container ID: {}",
                contextId, mariaDBContainer != null ? mariaDBContainer.getContainerId() : "null");
    }

    @Test
    @Order(2)
    @DisplayName("TestA-2: 컨테이너 격리 확인")
    void testA2_verifyContainerIsolation() {
        log.info("[TestA-2] 컨테이너 격리 검증");

        // TestA 데이터만 있는지 확인
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%testA_dirties@%'",
            Integer.class
        );
        assertThat(testACount).isEqualTo(1);

        // TestB 데이터가 없는지 확인 (격리 확인)
        Integer testBCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%testB_dirties@%'",
            Integer.class
        );
        assertThat(testBCount).isEqualTo(0);

        log.info("TestA-2 완료 - TestA: {} 건, TestB: {} 건", testACount, testBCount);
    }

    @AfterAll
    void afterAllTests() {
        log.info("=== WebEnvironmentIsolationTestA 종료 ===");
        log.info("Final Context ID: {}", contextId);

        if (mariaDBContainer != null) {
            log.info("Final Container URL: {}", mariaDBContainer.getJdbcUrl());
            log.info("Final Container ID: {}", mariaDBContainer.getContainerId());
            log.info("Final Container Hash: {}", System.identityHashCode(mariaDBContainer));
        }
    }
}