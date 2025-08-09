package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.MariaDbContainerSpec;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MariaDBContainer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MariaDB BeanCreator 실제 연결 테스트 (간단 버전)
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MariaDB BeanCreator Real Connection Tests")
class MariaDBBeanCreatorRealTest {

    private MariaDBBeanCreator beanCreator;
    private MariaDBContainer<?> container;
    private MariaDbContainerSpec spec;

    @BeforeAll
    void setUp() {
        beanCreator = new MariaDBBeanCreator();
        
        // MariaDB 컨테이너 생성
        container = new MariaDBContainer<>("mariadb:11.4.7")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        
        container.start();
        
        // Spec 설정
        spec = new MariaDbContainerSpec();
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setDatabase("testdb");
        spec.setMaxConnections(5);
        spec.setConnectionTimeout(10000);
        
        log.info("MariaDB 테스트 컨테이너 시작됨: {}", container.getJdbcUrl());
    }

    @AfterAll
    void tearDown() {
        if (container != null) {
            container.stop();
            log.info("MariaDB 테스트 컨테이너 중지됨");
        }
    }

    @Test
    @Order(1)
    @DisplayName("MariaDB BeanCreator 지원 타입 확인")
    void testSupportedType() {
        assertEquals(ContainerType.MARIADB, beanCreator.getSupportedType());
        log.info("✅ MariaDB BeanCreator 지원 타입: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("MariaDB DataSource 빈 생성 및 실제 연결 테스트")
    void testCreateBeanWithRealConnection() {
        // ContainerInfo 생성
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb",
                ContainerType.MARIADB,
                container,
                spec
        );

        // 실제 DataSource 생성 및 연결 테스트
        HikariDataSource dataSource = (HikariDataSource) assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        
        assertNotNull(dataSource, "생성된 DataSource가 null이 아니어야 합니다");
        assertInstanceOf(HikariDataSource.class, dataSource, "HikariDataSource 인스턴스여야 합니다");
        
        // 실제 데이터베이스 연결 테스트
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(5), "연결이 유효해야 합니다");
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT 1");
                assertTrue(resultSet.next(), "쿼리 결과가 있어야 합니다");
                assertEquals(1, resultSet.getInt(1), "결과값이 1이어야 합니다");
                log.info("✅ MariaDB 연결 테스트 성공");
            }
        }, "MariaDB 연결이 성공해야 합니다");
        
        // DataSource 종료
        dataSource.close();
        log.info("✅ MariaDB DataSource 생성 및 연결 테스트 성공");
    }

    @Test
    @Order(3)
    @DisplayName("MariaDB 기본값으로 DataSource 생성 테스트")
    void testCreateBeanWithDefaults() {
        // 기본 spec 사용
        MariaDbContainerSpec defaultSpec = new MariaDbContainerSpec();
        defaultSpec.setUsername("testuser");
        defaultSpec.setPassword("testpass");
        defaultSpec.setDatabase("testdb");
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-defaults",
                ContainerType.MARIADB,
                container,
                defaultSpec
        );

        HikariDataSource dataSource = (HikariDataSource) beanCreator.createBean(containerInfo);
        
        // 기본값으로 실제 연결 테스트
        assertNotNull(dataSource, "DataSource가 생성되어야 합니다");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(5), "기본값으로 연결이 유효해야 합니다");
                log.info("✅ 기본값으로 MariaDB 연결 테스트 성공");
            }
        });
        
        dataSource.close();
        log.info("✅ 기본값으로 MariaDB DataSource 생성 및 연결 테스트 성공");
    }
}