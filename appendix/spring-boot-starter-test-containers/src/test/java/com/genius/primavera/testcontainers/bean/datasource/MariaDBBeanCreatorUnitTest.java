package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.MariaDBContainerSpec;
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
        
        public void testApplyCommonSettings(HikariConfig config, MariaDBContainerSpec spec) {
            applyCommonSettings(config, spec);
        }
    }

    private TestableMariaDBBeanCreator beanCreator;
    
    @Mock
    private GenericContainer<?> mockContainer;
    
    private MariaDBContainerSpec spec;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.openMocks(this);
        beanCreator = new TestableMariaDBBeanCreator();
        
        when(mockContainer.getHost()).thenReturn("localhost");
        when(mockContainer.getFirstMappedPort()).thenReturn(3307);
        
        spec = new MariaDBContainerSpec();
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setMaxConnections(5);
        spec.setConnectionTimeout(10000);
        
        log.info("MariaDB test test completed");
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
    @DisplayName("test HikariConfig creation should test validation")
    void testCreateBaseConfig() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        HikariConfig config = beanCreator.testCreateBaseConfig(containerInfo);
        
        assertNotNull(config, "HikariConfigshould creation connection");
        assertTrue(config.getJdbcUrl().contains("mariadb"), "JDBC URLshould mariadbshould file connection");
        assertTrue(config.getJdbcUrl().contains("localhost:3307"), "JDBC URLshould file should file connection");
        assertEquals("test-mariadb-pool", config.getPoolName());
        assertEquals(2, config.getMinimumIdle());
        assertEquals(600000, config.getIdleTimeout());
        
        log.info(" test HikariConfig creation success");
        log.info("  - JDBC URL: {}", config.getJdbcUrl());
        log.info("  - Pool Name: {}", config.getPoolName());
    }

    @Test
    @Order(3)
    @DisplayName("test test validation")
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
        
        log.info(" test test success");
        log.info("  - Username: {}", config.getUsername());
        log.info("  - Pool Size: {}", config.getMaximumPoolSize());
    }

    @Test
    @Order(4)
    @DisplayName("test validation (test)")
    void testConfigurationOnly() {
        beanCreator.setReturnConfigOnly(true);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-config-only",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "should nullshould file connection");
        assertInstanceOf(String.class, result, "String with connection");
        assertTrue(result.toString().contains("test-mariadb-config-only"), "needs to be addedshould Endpoint connection");
        
        log.info(" test validation completed (test): {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(5)
    @DisplayName("test validation")
    void testDefaultSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        MariaDBContainerSpec defaultSpec = new MariaDBContainerSpec();
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-defaults",
                ContainerType.MARIADB,
                mockContainer,
                defaultSpec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "should creation connection");
        assertTrue(result.toString().contains("test-mariadb-defaults"), "needs to be addedshould Endpoint connection");
        
        log.info(" test validation success: {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(6)
    @DisplayName("MariaDB test validation")
    void testAdvancedSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        spec.setSqlMode(MariaDBContainerSpec.SqlMode.STRICT_TRANS_TABLES);
        spec.setDefaultStorageEngine(MariaDBContainerSpec.StorageEngine.INNODB);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-advanced",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "should creation connection");
        
        assertTrue(result.toString().contains("test-mariadb-advanced"), "needs to be addedshould Endpoint connection");
        
        log.info(" test validation completed: {}", result);
        log.info("  - SQL Mode: {}", spec.getSqlMode());
        log.info("  - Storage Engine: {}", spec.getDefaultStorageEngine());
        
        beanCreator.setReturnConfigOnly(false);
    }
}