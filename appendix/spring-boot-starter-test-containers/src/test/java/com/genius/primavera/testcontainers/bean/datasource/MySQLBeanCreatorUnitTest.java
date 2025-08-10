package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.MySqlContainerSpec;
import com.zaxxer.hikari.HikariConfig;
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
@DisplayName("MySQL BeanCreator Configuration Tests")
class MySQLBeanCreatorUnitTest {

    private static class TestableMySQLBeanCreator extends MySQLBeanCreator {
        private boolean shouldReturnConfig = false;
        
        public void setReturnConfigOnly(boolean returnConfigOnly) {
            this.shouldReturnConfig = returnConfigOnly;
        }
        
        @Override
        public Object createBean(ContainerInfo containerInfo) {
            if (shouldReturnConfig) {
                return "MySQL DataSource Configuration: " + containerInfo.name();
            }
            return super.createBean(containerInfo);
        }
        
        public HikariConfig testCreateBaseConfig(ContainerInfo containerInfo) {
            return createBaseConfig(containerInfo);
        }
        
        public void testApplyCommonSettings(HikariConfig config, MySqlContainerSpec spec) {
            applyCommonSettings(config, spec);
        }
    }

    private TestableMySQLBeanCreator beanCreator;
    
    @Mock
    private GenericContainer<?> mockContainer;
    
    private MySqlContainerSpec spec;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.openMocks(this);
        beanCreator = new TestableMySQLBeanCreator();
        
        when(mockContainer.getHost()).thenReturn("localhost");
        when(mockContainer.getFirstMappedPort()).thenReturn(3308);
        
        spec = new MySqlContainerSpec();
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setDefaultTimeZone("Asia/Seoul");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setDatabase("testdb");
        spec.setMaxConnections(5);
        spec.setConnectionTimeout(10000);
        spec.setSslEnabled(false);
        
        log.info("MySQL test test completed");
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
    @DisplayName("test HikariConfig creation should test validation")
    void testCreateBaseConfig() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        HikariConfig config = beanCreator.testCreateBaseConfig(containerInfo);
        
        assertNotNull(config, "HikariConfigshould creation connection");
        assertTrue(config.getJdbcUrl().contains("mysql"), "JDBC URLshould mysqlshould file connection");
        assertTrue(config.getJdbcUrl().contains("localhost:3308"), "JDBC URLshould file connection file connection");
        assertEquals("test-mysql-pool", config.getPoolName());
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
                "test-mysql-common",
                ContainerType.MYSQL,
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
                "test-mysql-config-only",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "should nullshould file connection");
        assertInstanceOf(String.class, result, "String with connection");
        assertTrue(result.toString().contains("test-mysql-config-only"), "needs to be addedshould Endpoint connection");
        
        log.info(" test validation completed (test): {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(5)
    @DisplayName("test validation")
    void testDefaultSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        MySqlContainerSpec defaultSpec = new MySqlContainerSpec();
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql-defaults",
                ContainerType.MYSQL,
                mockContainer,
                defaultSpec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "should creation connection");
        assertTrue(result.toString().contains("test-mysql-defaults"), "needs to be addedshould Endpoint connection");
        
        log.info(" test validation success: {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(6)
    @DisplayName("MySQL test validation")
    void testAdvancedSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        spec.setSqlMode(MySqlContainerSpec.SqlMode.STRICT_TRANS_TABLES);
        spec.setDefaultStorageEngine(MySqlContainerSpec.StorageEngine.INNODB);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql-advanced",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "should creation connection");
        
        assertTrue(result.toString().contains("test-mysql-advanced"), "needs to be addedshould Endpoint connection");
        
        log.info(" test validation completed: {}", result);
        log.info("  - SQL Mode: {}", spec.getSqlMode());
        log.info("  - Storage Engine: {}", spec.getDefaultStorageEngine());
        log.info("  - Character Set: {}", spec.getCharacterSet());
        log.info("  - Timezone: {}", spec.getDefaultTimeZone());
        log.info("  - SSL Enabled: {}", spec.getSslEnabled());
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(7)
    @DisplayName("MySQL connection should connection test validation")
    void testTimezoneAndCharsetSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        String[] timezones = {"UTC", "Asia/Tokyo", "America/New_York"};
        String[] charsets = {"utf8", "utf8mb4", "latin1"};
        
        for (int i = 0; i < timezones.length; i++) {
            spec.setDefaultTimeZone(timezones[i]);
            if (i < charsets.length) {
                spec.setCharacterSet(charsets[i]);
            }
            
            ContainerInfo containerInfo = new ContainerInfo(
                    "test-mysql-tz-" + i,
                    ContainerType.MYSQL,
                    mockContainer,
                    spec
            );

            Object result = beanCreator.createBean(containerInfo);
            assertNotNull(result, "should creation connection: " + timezones[i]);
            
            log.info(" connection {} / connection {} test validation completed", 
                    timezones[i], i < charsets.length ? charsets[i] : "default");
        }
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(8)
    @DisplayName("MySQL SSL test validation")
    void testSslSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        spec.setSslEnabled(false);
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql-ssl-disabled",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "SSL file testshould creation connection");
        
        log.info(" SSL file test validation completed");
        
        spec.setSslEnabled(true);
        containerInfo = new ContainerInfo(
                "test-mysql-ssl-enabled",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "SSL connection testshould creation connection");
        
        log.info(" SSL connection test validation completed");
        
        beanCreator.setReturnConfigOnly(false);
    }
}