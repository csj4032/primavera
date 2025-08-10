package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.MariaDbContainerSpec;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MariaDBContainer;

import static org.junit.jupiter.api.Assertions.*;

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
        
        container = new MariaDBContainer<>("mariadb:11.4.7")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        
        container.start();
        
        spec = new MariaDbContainerSpec();
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setDatabase("testdb");
        spec.setMaxConnections(5);
        spec.setConnectionTimeout(10000);
        
        log.info("MariaDB test file connection: {}", container.getJdbcUrl());
    }

    @AfterAll
    void tearDown() {
        if (container != null) {
            container.stop();
            log.info("MariaDB test file connection");
        }
    }

    @Test
    @Order(1)
    @DisplayName("MariaDB BeanCreator test verification")
    void testSupportedType() {
        assertEquals(ContainerType.MARIADB, beanCreator.getSupportedType());
        log.info(" MariaDB BeanCreator test: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("MariaDB DataSource should creation should test test")
    void testCreateBeanWithRealConnection() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb",
                ContainerType.MARIADB,
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
                log.info(" MariaDB test success");
            }
        }, "MariaDB testshould success connection");
        
        dataSource.close();
        log.info(" MariaDB DataSource creation should test success");
    }

    @Test
    @Order(3)
    @DisplayName("MariaDB Endpoint DataSource creation test")
    void testCreateBeanWithDefaults() {
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
        
        assertNotNull(dataSource, "DataSourceshould creation connection");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(5), "processing testshould file connection");
                log.info(" Endpoint MariaDB test success");
            }
        });
        
        dataSource.close();
        log.info(" Endpoint MariaDB DataSource creation should test success");
    }
}