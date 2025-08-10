package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.MySqlContainerSpec;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MySQL BeanCreator Real Connection Tests")
class MySQLBeanCreatorRealTest {

    private MySQLBeanCreator beanCreator;
    private MySQLContainer<?> container;
    private MySqlContainerSpec spec;

    @BeforeAll
    void setUp() {
        beanCreator = new MySQLBeanCreator();
        
        container = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        
        container.start();
        
        spec = new MySqlContainerSpec();
        spec.setCharacterSet("UTF-8");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setDefaultTimeZone("Asia/Seoul");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setDatabase("testdb");
        spec.setMaxConnections(5);
        spec.setConnectionTimeout(10000);
        spec.setSslEnabled(false);
        
        log.info("MySQL test file connection: {}", container.getJdbcUrl());
    }

    @AfterAll
    void tearDown() {
        if (container != null) {
            container.stop();
            log.info("MySQL test file connection");
        }
    }

    @Test
    @Order(1)
    @DisplayName("MySQL BeanCreator test verification")
    void testSupportedType() {
        assertEquals(ContainerType.MYSQL, beanCreator.getSupportedType());
        log.info(" MySQL BeanCreator test: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("MySQL DataSource should creation should test test")
    void testCreateBeanWithRealConnection() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql",
                ContainerType.MYSQL,
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
                log.info(" MySQL test success");
            }
        }, "MySQL testshould success connection");
        
        dataSource.close();
        log.info(" MySQL DataSource creation should test success");
    }

    @Test
    @Order(3)
    @DisplayName("MySQL SSL file test")
    void testSslDisabledSetting() {
        spec.setSslEnabled(false);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql-ssl-disabled",
                ContainerType.MYSQL,
                container,
                spec
        );

        HikariDataSource dataSource = (HikariDataSource) beanCreator.createBean(containerInfo);
        assertNotNull(dataSource, "SSL file DataSourceshould creation connection");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(3), "SSL file testshould file connection");
            }
        });
        dataSource.close();
        log.info(" SSL file test should test validation completed");
    }
}