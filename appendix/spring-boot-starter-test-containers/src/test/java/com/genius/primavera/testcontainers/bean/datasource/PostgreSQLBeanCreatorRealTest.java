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
        
        log.info("PostgreSQL test translated_text_4 translated_text_3: {}", container.getJdbcUrl());
    }

    @AfterAll
    void tearDown() {
        if (container != null) {
            container.stop();
            log.info("PostgreSQL test translated_text_4 translated_text_3");
        }
    }

    @Test
    @Order(1)
    @DisplayName("PostgreSQL BeanCreator translated_text_2 translated_text_2 verification")
    void testSupportedType() {
        assertEquals(ContainerType.POSTGRESQL, beanCreator.getSupportedType());
        log.info(" PostgreSQL BeanCreator translated_text_2 translated_text_2: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("PostgreSQL DataSource translated_text_1 creation translated_text_1 translated_text_2 translated_text_2 test")
    void testCreateBeanWithRealConnection() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql",
                ContainerType.POSTGRESQL,
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
                log.info(" PostgreSQL translated_text_2 test success");
            }
        }, "PostgreSQL translated_text_2translated_text_1 success translated_text_3");
        
        dataSource.close();
        log.info(" PostgreSQL DataSource creation translated_text_1 translated_text_2 test success");
    }

    @Test
    @Order(3)
    @DisplayName("PostgreSQL SSL translated_text_4 translated_text_2 test")
    void testSslDisabledSetting() {
        spec.setSslMode(PostgreSqlContainerSpec.SslMode.DISABLE);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql-ssl-disabled",
                ContainerType.POSTGRESQL,
                container,
                spec
        );

        HikariDataSource dataSource = (HikariDataSource) beanCreator.createBean(containerInfo);
        assertNotNull(dataSource, "DataSourcetranslated_text_1 creation translated_text_3");
        
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(3), "SSL translated_text_4 translated_text_2translated_text_1 translated_text_4 translated_text_3");
            }
        });
        
        dataSource.close();
        log.info(" SSL translated_text_4 translated_text_2 translated_text_1 translated_text_2 validation completed");
    }
}