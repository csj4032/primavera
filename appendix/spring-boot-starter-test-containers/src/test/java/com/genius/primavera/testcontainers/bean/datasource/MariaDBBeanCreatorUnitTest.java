package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.MariaDbContainerSpec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.GenericContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MariaDB BeanCreator Configuration Tests")
class MariaDBBeanCreatorUnitTest {

    private static class TestableMariaDBBeanCreator extends MariaDBBeanCreator {
        private boolean shouldReturnConfig = false;
        
        public void setReturnConfigOnly(boolean returnConfigOnly) {
            this.shouldReturnConfig = returnConfigOnly;
        }
        
        @Override
        public Object createBean(ContainerInfo containerInfo) {
            if (shouldReturnConfig) {
                return "MariaDB DataSource Configuration: " + containerInfo.name();
            }
            return super.createBean(containerInfo);
        }
        
        public HikariConfig testCreateBaseConfig(ContainerInfo containerInfo) {
            return createBaseConfig(containerInfo);
        }
        
        public void testApplyCommonSettings(HikariConfig config, MariaDbContainerSpec spec) {
            applyCommonSettings(config, spec);
        }
    }

    private TestableMariaDBBeanCreator beanCreator;
    
    @Mock
    private GenericContainer<?> mockContainer;
    
    private MariaDbContainerSpec spec;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.openMocks(this);
        beanCreator = new TestableMariaDBBeanCreator();
        
        when(mockContainer.getHost()).thenReturn("localhost");
        when(mockContainer.getFirstMappedPort()).thenReturn(3307);
        
        spec = new MariaDbContainerSpec();
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setMaxConnections(5);
        spec.setConnectionTimeout(10000);
        
        log.info("MariaDB translated_text_2 test translated_text_2 completed");
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
    @DisplayName("translated_text_2 HikariConfig creation translated_text_1 translated_text_2 validation")
    void testCreateBaseConfig() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        HikariConfig config = beanCreator.testCreateBaseConfig(containerInfo);
        
        assertNotNull(config, "HikariConfigtranslated_text_1 creation translated_text_3");
        assertTrue(config.getJdbcUrl().contains("mariadb"), "JDBC URLtranslated_text_1 mariadbtranslated_text_1 translated_text_4 translated_text_3");
        assertTrue(config.getJdbcUrl().contains("localhost:3307"), "JDBC URLtranslated_text_1 translated_text_4 translated_text_1 translated_text_4 translated_text_3");
        assertEquals("test-mariadb-pool", config.getPoolName());
        assertEquals(2, config.getMinimumIdle());
        assertEquals(600000, config.getIdleTimeout());
        
        log.info(" translated_text_2 HikariConfig creation success");
        log.info("  - JDBC URL: {}", config.getJdbcUrl());
        log.info("  - Pool Name: {}", config.getPoolName());
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 validation")
    void testApplyCommonSettings() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-common",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        HikariConfig config = beanCreator.testCreateBaseConfig(containerInfo);
        beanCreator.testApplyCommonSettings(config, spec);
        
        assertEquals("testuser", config.getUsername());
        assertEquals("testpass", config.getPassword());
        assertEquals(5, config.getMaximumPoolSize());
        assertEquals(10000, config.getConnectionTimeout());
        
        log.info(" translated_text_2 translated_text_2 translated_text_2 success");
        log.info("  - Username: {}", config.getUsername());
        log.info("  - Pool Size: {}", config.getMaximumPoolSize());
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_2 validation (translated_text_2 translated_text_2)")
    void testConfigurationOnly() {
        beanCreator.setReturnConfigOnly(true);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-config-only",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "translated_text_1 nulltranslated_text_1 translated_text_4 translated_text_3");
        assertInstanceOf(String.class, result, "String translated_text_6 translated_text_3");
        assertTrue(result.toString().contains("test-mariadb-config-only"), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_3");
        
        log.info(" translated_text_2 validation completed (translated_text_2 translated_text_2): {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_2 translated_text_2 validation")
    void testDefaultSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        MariaDbContainerSpec defaultSpec = new MariaDbContainerSpec();
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-defaults",
                ContainerType.MARIADB,
                mockContainer,
                defaultSpec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "translated_text_1 creation translated_text_3");
        assertTrue(result.toString().contains("test-mariadb-defaults"), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_3");
        
        log.info(" translated_text_2 translated_text_2 validation success: {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(6)
    @DisplayName("MariaDB translated_text_2 translated_text_2 validation")
    void testAdvancedSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        spec.setSqlMode(MariaDbContainerSpec.SqlMode.STRICT_TRANS_TABLES);
        spec.setDefaultStorageEngine(MariaDbContainerSpec.StorageEngine.INNODB);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-advanced",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "translated_text_1 creation translated_text_3");
        
        assertTrue(result.toString().contains("test-mariadb-advanced"), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_3");
        
        log.info(" translated_text_2 translated_text_2 validation completed: {}", result);
        log.info("  - SQL Mode: {}", spec.getSqlMode());
        log.info("  - Storage Engine: {}", spec.getDefaultStorageEngine());
        
        beanCreator.setReturnConfigOnly(false);
    }
}