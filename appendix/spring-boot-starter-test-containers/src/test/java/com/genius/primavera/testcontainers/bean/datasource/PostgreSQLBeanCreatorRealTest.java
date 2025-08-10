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
        
        log.info("PostgreSQL test file connection: {}", container.getJdbcUrl());
    }

    @AfterAll
    void tearDown() {
        if (container != null) {
            container.stop();
            log.info("PostgreSQL test file connection");
        }
    }

    @Test
    @Order(1)
    @DisplayName("PostgreSQL BeanCreator test verification")
    void testSupportedType() {
        assertEquals(ContainerType.POSTGRESQL, beanCreator.getSupportedType());
        log.info(" PostgreSQL BeanCreator test: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("PostgreSQL DataSource should creation should test test")
    void testCreateBeanWithRealConnection() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql",
                ContainerType.POSTGRESQL,
                container,
                spec
        );
        
        HikariDataSource dataSource = (HikariDataSource) assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        
        assertNotNull(dataSource, "creation DataSourceshould nullshould file connection");
        assertInstanceOf(HikariDataSource.class, dataSource, "HikariDataSource with connection");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(5), "testshould file connection");
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT 1");
                assertTrue(resultSet.next(), "test should connection");
                assertEquals(1, resultSet.getInt(1), "should 1should connection");
                log.info(" PostgreSQL test success");
            }
        }, "PostgreSQL testshould success connection");
        
        dataSource.close();
        log.info(" PostgreSQL DataSource creation should test success");
    }

    @Test
    @Order(3)
    @DisplayName("PostgreSQL SSL file test")
    void testSslDisabledSetting() {
        spec.setSslMode(PostgreSqlContainerSpec.SslMode.DISABLE);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql-ssl-disabled",
                ContainerType.POSTGRESQL,
                container,
                spec
        );

        HikariDataSource dataSource = (HikariDataSource) beanCreator.createBean(containerInfo);
        assertNotNull(dataSource, "DataSourceshould creation connection");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(3), "SSL file testshould file connection");
            }
        });
        
        dataSource.close();
        log.info(" SSL file test should test validation completed");
    }
}