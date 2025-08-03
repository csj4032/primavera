package com.genius.primavera.testcontainer.v2.extended;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: 어노테이션 방식으로 2개의 MariaDB 컨테이너와 각각 대응하는 JdbcTemplate 설정
 * @EnableTestContainers 어노테이션과 수동 DataSource 설정 조합
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MARIADB},  // 기본 컨테이너 1개는 어노테이션으로
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("어노테이션 방식 이중 MariaDB - 다중 DataSource")
class DualMariaDBAnnotationTest extends AutoDynamicPropertySource {

    // 두 번째 컨테이너는 수동으로 관리
    private static final org.testcontainers.containers.MariaDBContainer<?> secondaryMariaDB = 
            new org.testcontainers.containers.MariaDBContainer<>("mariadb:11.4.7")
                    .withDatabaseName("primavera_secondary")
                    .withUsername("secondary_user")
                    .withPassword("secondary_pass")
                    .withInitScript("init.sql");

    @DynamicPropertySource
    static void configureSecondaryProperties(DynamicPropertyRegistry registry) {
        // 두 번째 컨테이너 시작
        if (!secondaryMariaDB.isRunning()) {
            secondaryMariaDB.start();
        }
        
        // 두 번째 DataSource용 프로퍼티 등록
        registry.add("app.datasource.secondary.url", secondaryMariaDB::getJdbcUrl);
        registry.add("app.datasource.secondary.username", secondaryMariaDB::getUsername);
        registry.add("app.datasource.secondary.password", secondaryMariaDB::getPassword);
        registry.add("app.datasource.secondary.driver-class-name", secondaryMariaDB::getDriverClassName);
    }

    @TestConfiguration
    static class DualDataSourceConfig {
        
        // Primary DataSource는 @EnableTestContainers가 자동 설정
        // Secondary DataSource는 수동 설정
        @Bean("secondaryDataSource")
        public DataSource secondaryDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(secondaryMariaDB.getJdbcUrl());
            dataSource.setUsername(secondaryMariaDB.getUsername());
            dataSource.setPassword(secondaryMariaDB.getPassword());
            dataSource.setDriverClassName(secondaryMariaDB.getDriverClassName());
            return dataSource;
        }
        
        @Bean("secondaryJdbcTemplate")
        public JdbcTemplate secondaryJdbcTemplate(@Qualifier("secondaryDataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;  // Primary (어노테이션이 자동 설정)

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    private JdbcTemplate secondaryJdbcTemplate;  // Secondary (수동 설정)

    @Test
    @Order(1)
    @DisplayName("어노테이션 + 수동 설정 컨테이너 연결 확인")
    void testAnnotationAndManualContainers() {
        // Primary DB (어노테이션으로 설정된 컨테이너)
        Integer primaryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, primaryCount);
        
        // Secondary DB (수동으로 설정된 컨테이너)
        Integer secondaryCount = secondaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, secondaryCount);
        
        log.info("Annotation + Manual setup: primary={} users, secondary={} users", 
                primaryCount, secondaryCount);
    }

    @Test
    @Order(2)
    @DisplayName("각 컨테이너에 독립적인 데이터 처리")
    void testIndependentDataHandling() {
        // Primary에 데이터 삽입
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "primary-annotation@test.com", "{noop}password", "PrimaryAnnotationUser");
        
        // Secondary에 다른 데이터 삽입
        secondaryJdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "secondary-manual@test.com", "{noop}password", "SecondaryManualUser");
        
        // 독립성 확인
        Integer primaryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        Integer secondaryCount = secondaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        
        assertEquals(5, primaryCount);
        assertEquals(5, secondaryCount);
        
        // 교차 검증 - Primary에서 Secondary 데이터 검색
        Integer crossCheck1 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = 'secondary-manual@test.com'", Integer.class);
        assertEquals(0, crossCheck1);
        
        // 교차 검증 - Secondary에서 Primary 데이터 검색
        Integer crossCheck2 = secondaryJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = 'primary-annotation@test.com'", Integer.class);
        assertEquals(0, crossCheck2);
        
        log.info("Independent data handling verified: primary={}, secondary={}", 
                primaryCount, secondaryCount);
    }

    @Test
    @Order(3)
    @DisplayName("혼합 방식 성능 테스트")
    void testMixedApproachPerformance() {
        long startTime = System.currentTimeMillis();
        
        // Primary DB 작업 (어노테이션 설정)
        for (int i = 0; i < 50; i++) {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        }
        
        long midTime = System.currentTimeMillis();
        
        // Secondary DB 작업 (수동 설정)
        for (int i = 0; i < 50; i++) {
            secondaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        }
        
        long endTime = System.currentTimeMillis();
        
        long primaryDuration = midTime - startTime;
        long secondaryDuration = endTime - midTime;
        
        assertTrue(primaryDuration < 3000, "Primary (annotation) should be fast");
        assertTrue(secondaryDuration < 3000, "Secondary (manual) should be fast");
        
        log.info("Mixed approach performance: primary(annotation)={}ms, secondary(manual)={}ms", 
                primaryDuration, secondaryDuration);
    }

    @Test
    @Order(4)
    @DisplayName("컨테이너 상태 및 설정 확인")
    void testContainerStatusAndConfiguration() {
        // Primary 컨테이너 정보는 직접 접근하기 어려우므로 DB 쿼리로 확인
        String primaryVersion = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        assertTrue(primaryVersion.toLowerCase().contains("mariadb"));
        
        // Secondary 컨테이너 정보
        String secondaryVersion = secondaryJdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        assertTrue(secondaryVersion.toLowerCase().contains("mariadb"));
        assertTrue(secondaryMariaDB.isRunning());
        
        // 데이터베이스명 확인
        String secondaryDbName = secondaryJdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        assertEquals("primavera_secondary", secondaryDbName);
        
        log.info("Container status: primary version={}, secondary version={}, secondary DB={}", 
                primaryVersion.substring(0, Math.min(20, primaryVersion.length())),
                secondaryVersion.substring(0, Math.min(20, secondaryVersion.length())),
                secondaryDbName);
    }

    @AfterAll
    static void cleanup() {
        if (secondaryMariaDB.isRunning()) {
            secondaryMariaDB.stop();
        }
    }
}