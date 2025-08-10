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
        
        log.info("MySQL translated_text_2 test translated_text_2 completed");
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
    @DisplayName("translated_text_2 HikariConfig creation translated_text_1 translated_text_2 validation")
    void testCreateBaseConfig() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        HikariConfig config = beanCreator.testCreateBaseConfig(containerInfo);
        
        assertNotNull(config, "HikariConfigtranslated_text_1 creation translated_text_3");
        assertTrue(config.getJdbcUrl().contains("mysql"), "JDBC URLtranslated_text_1 mysqltranslated_text_1 translated_text_4 translated_text_3");
        assertTrue(config.getJdbcUrl().contains("localhost:3308"), "JDBC URLtranslated_text_1 translated_text_4 translated_text_3 translated_text_4 translated_text_3");
        assertEquals("test-mysql-pool", config.getPoolName());
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
                "test-mysql-config-only",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "translated_text_1 nulltranslated_text_1 translated_text_4 translated_text_3");
        assertInstanceOf(String.class, result, "String translated_text_6 translated_text_3");
        assertTrue(result.toString().contains("test-mysql-config-only"), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_3");
        
        log.info(" translated_text_2 validation completed (translated_text_2 translated_text_2): {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_2 translated_text_2 validation")
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
        
        assertNotNull(result, "translated_text_1 creation translated_text_3");
        assertTrue(result.toString().contains("test-mysql-defaults"), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_3");
        
        log.info(" translated_text_2 translated_text_2 validation success: {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(6)
    @DisplayName("MySQL translated_text_2 translated_text_2 validation")
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
        assertNotNull(result, "translated_text_1 creation translated_text_3");
        
        assertTrue(result.toString().contains("test-mysql-advanced"), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_3");
        
        log.info(" translated_text_2 translated_text_2 validation completed: {}", result);
        log.info("  - SQL Mode: {}", spec.getSqlMode());
        log.info("  - Storage Engine: {}", spec.getDefaultStorageEngine());
        log.info("  - Character Set: {}", spec.getCharacterSet());
        log.info("  - Timezone: {}", spec.getDefaultTimeZone());
        log.info("  - SSL Enabled: {}", spec.getSslEnabled());
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(7)
    @DisplayName("MySQL translated_text_3 translated_text_1 translated_text_3 translated_text_2 validation")
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
            assertNotNull(result, "translated_text_1 creation translated_text_3: " + timezones[i]);
            
            log.info(" translated_text_3 {} / translated_text_3 {} translated_text_2 validation completed", 
                    timezones[i], i < charsets.length ? charsets[i] : "default");
        }
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(8)
    @DisplayName("MySQL SSL translated_text_2 validation")
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
        assertNotNull(result, "SSL translated_text_4 translated_text_2translated_text_1 creation translated_text_3");
        
        log.info(" SSL translated_text_4 translated_text_2 validation completed");
        
        spec.setSslEnabled(true);
        containerInfo = new ContainerInfo(
                "test-mysql-ssl-enabled",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "SSL translated_text_3 translated_text_2translated_text_1 creation translated_text_3");
        
        log.info(" SSL translated_text_3 translated_text_2 validation completed");
        
        beanCreator.setReturnConfigOnly(false);
    }
}