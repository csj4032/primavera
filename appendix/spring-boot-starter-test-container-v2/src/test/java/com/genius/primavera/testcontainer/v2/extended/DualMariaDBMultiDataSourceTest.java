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
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: 2개의 MariaDB 컨테이너와 각각에 대응하는 JdbcTemplate 설정
 * 수동 컨테이너 관리를 통한 다중 데이터소스 구성
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("이중 MariaDB 컨테이너 - 다중 DataSource 설정")
class DualMariaDBMultiDataSourceTest {

    @Container
    static MariaDBContainer<?> mariadb1 = new MariaDBContainer<>("mariadb:11.4.7")
            .withDatabaseName("primavera_db1")
            .withUsername("user1")
            .withPassword("pass1")
            .withInitScript("init.sql");

    @Container 
    static MariaDBContainer<?> mariadb2 = new MariaDBContainer<>("mariadb:11.4.7")
            .withDatabaseName("primavera_db2")
            .withUsername("user2")
            .withPassword("pass2")
            .withInitScript("init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Primary DataSource (mariadb1)
        registry.add("spring.datasource.url", mariadb1::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb1::getUsername);
        registry.add("spring.datasource.password", mariadb1::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb1::getDriverClassName);
        
        // Secondary DataSource (mariadb2) - 커스텀 속성으로 등록
        registry.add("app.datasource.secondary.url", mariadb2::getJdbcUrl);
        registry.add("app.datasource.secondary.username", mariadb2::getUsername);
        registry.add("app.datasource.secondary.password", mariadb2::getPassword);
        registry.add("app.datasource.secondary.driver-class-name", mariadb2::getDriverClassName);
    }

    @TestConfiguration
    static class MultiDataSourceConfig {
        
        @Bean
        @Primary
        public DataSource primaryDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(mariadb1.getJdbcUrl());
            dataSource.setUsername(mariadb1.getUsername());
            dataSource.setPassword(mariadb1.getPassword());
            dataSource.setDriverClassName(mariadb1.getDriverClassName());
            return dataSource;
        }
        
        @Bean("secondaryDataSource")
        public DataSource secondaryDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(mariadb2.getJdbcUrl());
            dataSource.setUsername(mariadb2.getUsername());
            dataSource.setPassword(mariadb2.getPassword());
            dataSource.setDriverClassName(mariadb2.getDriverClassName());
            return dataSource;
        }
        
        @Bean
        @Primary
        public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
        
        @Bean("secondaryJdbcTemplate")
        public JdbcTemplate secondaryJdbcTemplate(@Qualifier("secondaryDataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }

    @Autowired
    private JdbcTemplate primaryJdbcTemplate;

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    private JdbcTemplate secondaryJdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("두 MariaDB 컨테이너 연결 확인")
    void testDualContainerConnections() {
        // Primary DB 연결 확인
        Integer primaryCount = primaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, primaryCount);
        
        // Secondary DB 연결 확인
        Integer secondaryCount = secondaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, secondaryCount);
        
        log.info("Dual MariaDB connections: primary={} users, secondary={} users", 
                primaryCount, secondaryCount);
    }

    @Test
    @Order(2)
    @DisplayName("각 DB에 독립적인 데이터 삽입")
    void testIndependentDataOperations() {
        // Primary DB에 데이터 삽입
        primaryJdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "primary@dual.com", "{noop}password", "PrimaryUser");
        
        // Secondary DB에 다른 데이터 삽입
        secondaryJdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "secondary@dual.com", "{noop}password", "SecondaryUser");
        
        // 각 DB의 데이터 확인
        Integer primaryCount = primaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        Integer secondaryCount = secondaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        
        assertEquals(5, primaryCount);
        assertEquals(5, secondaryCount);
        
        // Primary DB에서는 secondary 데이터가 보이지 않아야 함
        Integer primarySecondaryCount = primaryJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = 'secondary@dual.com'", Integer.class);
        assertEquals(0, primarySecondaryCount);
        
        // Secondary DB에서는 primary 데이터가 보이지 않아야 함
        Integer secondaryPrimaryCount = secondaryJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = 'primary@dual.com'", Integer.class);
        assertEquals(0, secondaryPrimaryCount);
        
        log.info("Independent operations: primary={} users, secondary={} users", 
                primaryCount, secondaryCount);
    }

    @Test
    @Order(3)
    @DisplayName("두 DB 간 트랜잭션 독립성 확인")
    void testTransactionIsolation() {
        // Primary DB에서 트랜잭션 실행
        try {
            primaryJdbcTemplate.execute("START TRANSACTION");
            primaryJdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                    "tx-primary@dual.com", "{noop}password", "TxPrimaryUser");
            
            // Secondary DB에서는 이 변경사항이 보이지 않아야 함
            Integer secondaryTxCount = secondaryJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM USERS WHERE EMAIL = 'tx-primary@dual.com'", Integer.class);
            assertEquals(0, secondaryTxCount);
            
            primaryJdbcTemplate.execute("COMMIT");
            
        } catch (Exception e) {
            primaryJdbcTemplate.execute("ROLLBACK");
            throw e;
        }
        
        // Secondary DB에서 독립적인 작업
        secondaryJdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "tx-secondary@dual.com", "{noop}password", "TxSecondaryUser");
        
        // 최종 확인
        Integer primaryFinalCount = primaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        Integer secondaryFinalCount = secondaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        
        log.info("Transaction isolation: primary={} users, secondary={} users", 
                primaryFinalCount, secondaryFinalCount);
    }

    @Test
    @Order(4)
    @DisplayName("컨테이너별 성능 비교")
    void testPerformanceComparison() {
        // Primary DB 성능 측정
        long primaryStart = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            primaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        }
        long primaryDuration = System.currentTimeMillis() - primaryStart;
        
        // Secondary DB 성능 측정
        long secondaryStart = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            secondaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        }
        long secondaryDuration = System.currentTimeMillis() - secondaryStart;
        
        assertTrue(primaryDuration < 5000, "Primary DB should respond within 5 seconds");
        assertTrue(secondaryDuration < 5000, "Secondary DB should respond within 5 seconds");
        
        log.info("Performance comparison: primary={}ms, secondary={}ms", 
                primaryDuration, secondaryDuration);
    }
}