package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.PostgreSqlContainerSpec;
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
@DisplayName("PostgreSQL BeanCreator Configuration Tests")
class PostgreSQLBeanCreatorUnitTest {

    private static class TestablePostgreSQLBeanCreator extends PostgreSQLBeanCreator {
        private boolean shouldReturnConfig = false;
        
        public void setReturnConfigOnly(boolean returnConfigOnly) {
            this.shouldReturnConfig = returnConfigOnly;
        }
        
        @Override
        public Object createBean(ContainerInfo containerInfo) {
            if (shouldReturnConfig) {
                return "PostgreSQL DataSource Configuration: " + containerInfo.name();
            }
            return super.createBean(containerInfo);
        }
        
        public HikariConfig testCreateBaseConfig(ContainerInfo containerInfo) {
            return createBaseConfig(containerInfo);
        }
        
        public void testApplyCommonSettings(HikariConfig config, PostgreSqlContainerSpec spec) {
            applyCommonSettings(config, spec);
        }
    }

    private TestablePostgreSQLBeanCreator beanCreator;
    
    @Mock
    private GenericContainer<?> mockContainer;
    
    private PostgreSqlContainerSpec spec;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.openMocks(this);
        beanCreator = new TestablePostgreSQLBeanCreator();
        
        when(mockContainer.getHost()).thenReturn("localhost");
        when(mockContainer.getFirstMappedPort()).thenReturn(5433);
        
        spec = new PostgreSqlContainerSpec();
        spec.setLocale("en_US.UTF-8");
        spec.setEncoding("UTF8");
        spec.setTimezone("Asia/Seoul");
        spec.setDateStyle("ISO, YMD");
        spec.setSslMode(PostgreSqlContainerSpec.SslMode.DISABLE);
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setDatabase("testdb");
        spec.setMaxConnections(5);
        spec.setConnectionTimeout(10000);
        
        log.info("PostgreSQL translated_text_2 test translated_text_2 completed");
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
    @DisplayName("translated_text_2 HikariConfig creation translated_text_1 translated_text_2 validation")
    void testCreateBaseConfig() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql",
                ContainerType.POSTGRESQL,
                mockContainer,
                spec
        );

        HikariConfig config = beanCreator.testCreateBaseConfig(containerInfo);
        
        assertNotNull(config, "HikariConfigtranslated_text_1 creation translated_text_3");
        assertTrue(config.getJdbcUrl().contains("postgresql"), "JDBC URLtranslated_text_1 postgresqltranslated_text_1 translated_text_4 translated_text_3");
        assertTrue(config.getJdbcUrl().contains("localhost:5433"), "JDBC URLtranslated_text_1 translated_text_4 translated_text_3 translated_text_4 translated_text_3");
        assertEquals("test-postgresql-pool", config.getPoolName());
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
                "test-postgresql-common",
                ContainerType.POSTGRESQL,
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
                "test-postgresql-config-only",
                ContainerType.POSTGRESQL,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "translated_text_1 nulltranslated_text_1 translated_text_4 translated_text_3");
        assertInstanceOf(String.class, result, "String translated_text_6 translated_text_3");
        assertTrue(result.toString().contains("test-postgresql-config-only"), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_3");
        
        log.info(" translated_text_2 validation completed (translated_text_2 translated_text_2): {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_2 translated_text_2 validation")
    void testDefaultSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        PostgreSqlContainerSpec defaultSpec = new PostgreSqlContainerSpec();
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql-defaults",
                ContainerType.POSTGRESQL,
                mockContainer,
                defaultSpec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "translated_text_1 creation translated_text_3");
        assertTrue(result.toString().contains("test-postgresql-defaults"), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_3");
        
        log.info(" translated_text_2 translated_text_2 validation success: {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(6)
    @DisplayName("PostgreSQL translated_text_2 translated_text_2 validation")
    void testAdvancedSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        spec.setSslMode(PostgreSqlContainerSpec.SslMode.REQUIRE);
        spec.setSharedBuffers("256MB");
        spec.setWorkMem("8MB");
        spec.setMaxConnections(100);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql-advanced",
                ContainerType.POSTGRESQL,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "translated_text_1 creation translated_text_3");
        
        assertTrue(result.toString().contains("test-postgresql-advanced"), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_3");
        
        log.info(" translated_text_2 translated_text_2 validation completed: {}", result);
        log.info("  - SSL Mode: {}", spec.getSslMode());
        log.info("  - Shared Buffers: {}", spec.getSharedBuffers());
        log.info("  - Work Mem: {}", spec.getWorkMem());
        log.info("  - Max Connections: {}", spec.getMaxConnections());
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(7)
    @DisplayName("PostgreSQL SSL translated_text_2 translated_text_2 validation")
    void testSslModeSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        PostgreSqlContainerSpec.SslMode[] sslModes = {
            PostgreSqlContainerSpec.SslMode.DISABLE,
            PostgreSqlContainerSpec.SslMode.PREFER,
            PostgreSqlContainerSpec.SslMode.REQUIRE
        };
        
        for (PostgreSqlContainerSpec.SslMode sslMode : sslModes) {
            spec.setSslMode(sslMode);
            
            ContainerInfo containerInfo = new ContainerInfo(
                    "test-postgresql-ssl-" + sslMode.name().toLowerCase(),
                    ContainerType.POSTGRESQL,
                    mockContainer,
                    spec
            );

            Object result = beanCreator.createBean(containerInfo);
            assertNotNull(result, "translated_text_1 creation translated_text_3: " + sslMode);
            
            log.info(" SSL translated_text_2 {} translated_text_2 validation completed", sslMode);
        }
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(8)
    @DisplayName("PostgreSQL translated_text_3 translated_text_1 translated_text_3 translated_text_2 validation")
    void testLocaleAndEncodingSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        String[] locales = {"en_US.UTF-8", "ko_KR.UTF-8", "ja_JP.UTF-8"};
        String[] encodings = {"UTF8", "LATIN1", "EUC_KR"};
        
        for (int i = 0; i < locales.length; i++) {
            spec.setLocale(locales[i]);
            if (i < encodings.length) {
                spec.setEncoding(encodings[i]);
            }
            
            ContainerInfo containerInfo = new ContainerInfo(
                    "test-postgresql-locale-" + i,
                    ContainerType.POSTGRESQL,
                    mockContainer,
                    spec
            );

            Object result = beanCreator.createBean(containerInfo);
            assertNotNull(result, "translated_text_1 creation translated_text_3");
            
            log.info(" translated_text_3 {} / translated_text_3 {} translated_text_2 validation completed", 
                    locales[i], i < encodings.length ? encodings[i] : "default");
        }
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(9)
    @DisplayName("PostgreSQL translated_text_3 translated_text_1 translated_text_2 translated_text_2 validation")
    void testMemoryAndPerformanceSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        spec.setSharedBuffers("512MB");
        spec.setWorkMem("16MB");
        spec.setMaintenanceWorkMem("128MB");
        spec.setWalBuffers("32MB");
        spec.setMaxConnections(200);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql-performance",
                ContainerType.POSTGRESQL,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "translated_text_1 creation translated_text_3");
        
        log.info(" translated_text_3 translated_text_1 translated_text_2 translated_text_2 validation completed: {}", result);
        log.info("  - Shared Buffers: {}", spec.getSharedBuffers());
        log.info("  - Work Mem: {}", spec.getWorkMem());
        log.info("  - Maintenance Work Mem: {}", spec.getMaintenanceWorkMem());
        log.info("  - WAL Buffers: {}", spec.getWalBuffers());
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(10)
    @DisplayName("PostgreSQL translated_text_3 translated_text_1 translated_text_2 translated_text_3 translated_text_2 validation")
    void testTimezoneAndDateStyleSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        String[] timezones = {"UTC", "Asia/Tokyo", "America/New_York", "Europe/London"};
        String[] dateStyles = {"ISO, YMD", "German, DMY", "US, MDY"};
        
        for (int i = 0; i < timezones.length; i++) {
            spec.setTimezone(timezones[i]);
            if (i < dateStyles.length) {
                spec.setDateStyle(dateStyles[i]);
            }
            
            ContainerInfo containerInfo = new ContainerInfo(
                    "test-postgresql-tz-" + i,
                    ContainerType.POSTGRESQL,
                    mockContainer,
                    spec
            );

            Object result = beanCreator.createBean(containerInfo);
            assertNotNull(result, "translated_text_1 creation translated_text_3: " + timezones[i]);
            
            log.info(" translated_text_3 {} / translated_text_2 translated_text_3 {} translated_text_2 validation completed", 
                    timezones[i], i < dateStyles.length ? dateStyles[i] : "default");
        }
        
        beanCreator.setReturnConfigOnly(false);
    }
}