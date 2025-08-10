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
        
        log.info("MariaDB test translated_text_4 translated_text_3: {}", container.getJdbcUrl());
    }

    @AfterAll
    void tearDown() {
        if (container != null) {
            container.stop();
            log.info("MariaDB test translated_text_4 translated_text_3");
        }
    }

    @Test
    @Order(1)
    @DisplayName("MariaDB BeanCreator translated_text_2 translated_text_2 verification")
    void testSupportedType() {
        assertEquals(ContainerType.MARIADB, beanCreator.getSupportedType());
        log.info(" MariaDB BeanCreator translated_text_2 translated_text_2: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("MariaDB DataSource translated_text_1 creation translated_text_1 translated_text_2 translated_text_2 test")
    void testCreateBeanWithRealConnection() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb",
                ContainerType.MARIADB,
                container,
                spec
        );

        HikariDataSource dataSource = (HikariDataSource) assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        
        assertNotNull(dataSource, "creation DataSourcetranslated_text_1 nulltranslated_text_1 translated_text_4 translated_text_3");
        assertInstanceOf(HikariDataSource.class, dataSource, "HikariDataSource translated_text_6 translated_text_3");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(5), "translated_text_2translated_text_1 translated_text_4 translated_text_3");
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT 1");
                assertTrue(resultSet.next(), "translated_text_2 translated_text_1 translated_text_3 translated_text_3");
                assertEquals(1, resultSet.getInt(1), "translated_text_1 1translated_text_1 translated_text_3");
                log.info(" MariaDB translated_text_2 test success");
            }
        }, "MariaDB translated_text_2translated_text_1 success translated_text_3");
        
        dataSource.close();
        log.info(" MariaDB DataSource creation translated_text_1 translated_text_2 test success");
    }

    @Test
    @Order(3)
    @DisplayName("MariaDB translated_text_5 DataSource creation test")
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
        
        assertNotNull(dataSource, "DataSourcetranslated_text_1 creation translated_text_3");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(5), "translated_text_5 translated_text_2translated_text_1 translated_text_4 translated_text_3");
                log.info(" translated_text_5 MariaDB translated_text_2 test success");
            }
        });
        
        dataSource.close();
        log.info(" translated_text_5 MariaDB DataSource creation translated_text_1 translated_text_2 test success");
    }
}