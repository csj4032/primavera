package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.PostgreSqlContainerSpec;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PostgreSQL BeanCreator Real Connection Tests")
class PostgreSQLBeanCreatorRealTest {

    private PostgreSQLBeanCreator beanCreator;
    private PostgreSQLContainer<?> container;
    private PostgreSqlContainerSpec spec;

    @BeforeAll
    void setUp() {
        beanCreator = new PostgreSQLBeanCreator();
        
        container = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        
        container.start();
        
        spec = new PostgreSqlContainerSpec();
        spec.setLocale("en_US.UTF-8");
        spec.setEncoding("UTF8");
        spec.setTimezone("Asia/Seoul");
        spec.setDateStyle("ISO");
        spec.setSslMode(PostgreSqlContainerSpec.SslMode.DISABLE);
        spec.setDatabase("testdb");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setMaxConnections(5);
        spec.setConnectionTimeout(10000);
        
        log.info("PostgreSQL 테스트 컨테이너 시작됨: {}", container.getJdbcUrl());
    }

    @AfterAll
    void tearDown() {
        if (container != null) {
            container.stop();
            log.info("PostgreSQL 테스트 컨테이너 중지됨");
        }
    }

    @Test
    @Order(1)
    @DisplayName("PostgreSQL BeanCreator 지원 타입 확인")
    void testSupportedType() {
        assertEquals(ContainerType.POSTGRESQL, beanCreator.getSupportedType());
        log.info("✅ PostgreSQL BeanCreator 지원 타입: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("PostgreSQL DataSource 빈 생성 및 실제 연결 테스트")
    void testCreateBeanWithRealConnection() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql",
                ContainerType.POSTGRESQL,
                container,
                spec
        );
        
        HikariDataSource dataSource = (HikariDataSource) assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        
        assertNotNull(dataSource, "생성된 DataSource가 null이 아니어야 합니다");
        assertInstanceOf(HikariDataSource.class, dataSource, "HikariDataSource 인스턴스여야 합니다");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(5), "연결이 유효해야 합니다");
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT 1");
                assertTrue(resultSet.next(), "쿼리 결과가 있어야 합니다");
                assertEquals(1, resultSet.getInt(1), "결과값이 1이어야 합니다");
                log.info("✅ PostgreSQL 연결 테스트 성공");
            }
        }, "PostgreSQL 연결이 성공해야 합니다");
        
        dataSource.close();
        log.info("✅ PostgreSQL DataSource 생성 및 연결 테스트 성공");
    }

    @Test
    @Order(3)
    @DisplayName("PostgreSQL SSL 비활성화 설정 테스트")
    void testSslDisabledSetting() {
        spec.setSslMode(PostgreSqlContainerSpec.SslMode.DISABLE);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql-ssl-disabled",
                ContainerType.POSTGRESQL,
                container,
                spec
        );

        HikariDataSource dataSource = (HikariDataSource) beanCreator.createBean(containerInfo);
        assertNotNull(dataSource, "DataSource가 생성되어야 합니다");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(3), "SSL 비활성화 연결이 유효해야 합니다");
            }
        });
        
        dataSource.close();
        log.info("✅ SSL 비활성화 설정 및 연결 검증 완료");
    }
}