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
        
        log.info("MySQL test translated_text_4 translated_text_3: {}", container.getJdbcUrl());
    }

    @AfterAll
    void tearDown() {
        if (container != null) {
            container.stop();
            log.info("MySQL test translated_text_4 translated_text_3");
        }
    }

    @Test
    @Order(1)
    @DisplayName("MySQL BeanCreator translated_text_2 translated_text_2 verification")
    void testSupportedType() {
        assertEquals(ContainerType.MYSQL, beanCreator.getSupportedType());
        log.info(" MySQL BeanCreator translated_text_2 translated_text_2: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("MySQL DataSource translated_text_1 creation translated_text_1 translated_text_2 translated_text_2 test")
    void testCreateBeanWithRealConnection() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql",
                ContainerType.MYSQL,
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
                log.info(" MySQL translated_text_2 test success");
            }
        }, "MySQL translated_text_2translated_text_1 success translated_text_3");
        
        dataSource.close();
        log.info(" MySQL DataSource creation translated_text_1 translated_text_2 test success");
    }

    @Test
    @Order(3)
    @DisplayName("MySQL SSL translated_text_4 translated_text_2 test")
    void testSslDisabledSetting() {
        spec.setSslEnabled(false);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql-ssl-disabled",
                ContainerType.MYSQL,
                container,
                spec
        );

        HikariDataSource dataSource = (HikariDataSource) beanCreator.createBean(containerInfo);
        assertNotNull(dataSource, "SSL translated_text_4 DataSourcetranslated_text_1 creation translated_text_3");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(3), "SSL translated_text_4 translated_text_2translated_text_1 translated_text_4 translated_text_3");
            }
        });
        dataSource.close();
        log.info(" SSL translated_text_4 translated_text_2 translated_text_1 translated_text_2 validation completed");
    }
}