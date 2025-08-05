package com.genius.primavera.testcontainer.isolation;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 간단한 ApplicationContext 격리 테스트 A
 * Spring Boot의 @DirtiesContext를 사용한 컨텍스트 격리 시도
 */
@Slf4j
@SpringBootTest
@EnableTestContainers(containers = ContainerType.MARIADB)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) 
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("간단한 컨텍스트 격리 테스트 A")
class SimpleContextIsolationTestA {

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
        log.info("=== SimpleContextIsolationTestA 시작 ===");
        log.info("ApplicationContext ID: {}", contextId);
        log.info("ApplicationContext Hash: {}", applicationContext.hashCode());
        log.info("ApplicationContext Class: {}", applicationContext.getClass().getSimpleName());
        
        if (mariaDBContainer != null) {
            log.info("TestA Container URL: {}", mariaDBContainer.getJdbcUrl());
            log.info("TestA Container ID: {}", mariaDBContainer.getContainerId());
            log.info("TestA Container Port: {}", mariaDBContainer.getMappedPort(3306));
        }
    }

    @Test
    @Order(1)
    @DisplayName("TestA-1: ApplicationContext 정보 확인")
    void testA1_verifyApplicationContext() {
        log.info("[TestA-1] ApplicationContext 검증");
        log.info("Current Context ID: {}", System.identityHashCode(applicationContext));
        log.info("Current Context Hash: {}", applicationContext.hashCode());
        
        // 고유 데이터 삽입
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('TestA Context User', 'testA_context@example.com')");
        
        // 데이터 확인
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%testA_context@%'", 
            Integer.class
        );
        assertThat(count).isEqualTo(1);
        
        log.info("TestA-1 완료 - Context ID: {}", contextId);
    }

    @Test
    @Order(2)
    @DisplayName("TestA-2: 데이터 격리 확인")
    void testA2_verifyDataIsolation() {
        log.info("[TestA-2] 데이터 격리 검증");
        
        // TestA 데이터만 있는지 확인
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%testA_context@%'", 
            Integer.class
        );
        assertThat(testACount).isEqualTo(1);
        
        // TestB 데이터가 없는지 확인 (격리 확인)
        Integer testBCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%testB_context@%'", 
            Integer.class
        );
        assertThat(testBCount).isEqualTo(0);
        
        log.info("TestA-2 완료 - TestA: {} 건, TestB: {} 건", testACount, testBCount);
    }

    @AfterAll
    void afterAllTests() {
        log.info("=== SimpleContextIsolationTestA 종료 ===");
        log.info("Final Context ID: {}", contextId);
        
        if (mariaDBContainer != null) {
            log.info("Final Container URL: {}", mariaDBContainer.getJdbcUrl());
            log.info("Final Container ID: {}", mariaDBContainer.getContainerId());
        }
    }
}