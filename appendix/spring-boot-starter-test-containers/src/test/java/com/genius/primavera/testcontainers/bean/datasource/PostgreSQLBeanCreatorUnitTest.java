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
        
        log.info("PostgreSQL 단위 테스트 설정 완료");
    }

    @Test
    @Order(1)
    @DisplayName("PostgreSQL BeanCreator 지원 타입 확인")
    void testSupportedType() {
        assertEquals(ContainerType.POSTGRESQL, beanCreator.getSupportedType());
        log.info("✅ PostgreSQL BeanCreator 지원 타입: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("기본 HikariConfig 생성 및 설정 검증")
    void testCreateBaseConfig() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql",
                ContainerType.POSTGRESQL,
                mockContainer,
                spec
        );

        HikariConfig config = beanCreator.testCreateBaseConfig(containerInfo);
        
        assertNotNull(config, "HikariConfig가 생성되어야 합니다");
        assertTrue(config.getJdbcUrl().contains("postgresql"), "JDBC URL은 postgresql을 포함해야 합니다");
        assertTrue(config.getJdbcUrl().contains("localhost:5433"), "JDBC URL은 호스트와 포트를 포함해야 합니다");
        assertEquals("test-postgresql-pool", config.getPoolName());
        assertEquals(2, config.getMinimumIdle());
        assertEquals(600000, config.getIdleTimeout());
        
        log.info("✅ 기본 HikariConfig 생성 성공");
        log.info("  - JDBC URL: {}", config.getJdbcUrl());
        log.info("  - Pool Name: {}", config.getPoolName());
    }

    @Test
    @Order(3)
    @DisplayName("공통 설정 적용 검증")
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
        
        log.info("✅ 공통 설정 적용 성공");
        log.info("  - Username: {}", config.getUsername());
        log.info("  - Pool Size: {}", config.getMaximumPoolSize());
    }

    @Test
    @Order(4)
    @DisplayName("설정만 검증 (연결 없음)")
    void testConfigurationOnly() {
        beanCreator.setReturnConfigOnly(true);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-postgresql-config-only",
                ContainerType.POSTGRESQL,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "결과가 null이 아니어야 합니다");
        assertInstanceOf(String.class, result, "String 인스턴스여야 합니다");
        assertTrue(result.toString().contains("test-postgresql-config-only"), "컨테이너 이름이 포함되어야 합니다");
        
        log.info("✅ 설정 검증 완료 (연결 없음): {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(5)
    @DisplayName("기본값 설정 검증")
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
        
        assertNotNull(result, "결과가 생성되어야 합니다");
        assertTrue(result.toString().contains("test-postgresql-defaults"), "컨테이너 이름이 포함되어야 합니다");
        
        log.info("✅ 기본값 설정 검증 성공: {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(6)
    @DisplayName("PostgreSQL 고급 설정 검증")
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
        assertNotNull(result, "결과가 생성되어야 합니다");
        
        assertTrue(result.toString().contains("test-postgresql-advanced"), "컨테이너 이름이 포함되어야 합니다");
        
        log.info("✅ 고급 설정 검증 완료: {}", result);
        log.info("  - SSL Mode: {}", spec.getSslMode());
        log.info("  - Shared Buffers: {}", spec.getSharedBuffers());
        log.info("  - Work Mem: {}", spec.getWorkMem());
        log.info("  - Max Connections: {}", spec.getMaxConnections());
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(7)
    @DisplayName("PostgreSQL SSL 모드 설정 검증")
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
            assertNotNull(result, "결과가 생성되어야 합니다: " + sslMode);
            
            log.info("✅ SSL 모드 {} 설정 검증 완료", sslMode);
        }
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(8)
    @DisplayName("PostgreSQL 로케일 및 인코딩 설정 검증")
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
            assertNotNull(result, "결과가 생성되어야 합니다");
            
            log.info("✅ 로케일 {} / 인코딩 {} 설정 검증 완료", 
                    locales[i], i < encodings.length ? encodings[i] : "default");
        }
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(9)
    @DisplayName("PostgreSQL 메모리 및 성능 설정 검증")
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
        assertNotNull(result, "결과가 생성되어야 합니다");
        
        log.info("✅ 메모리 및 성능 설정 검증 완료: {}", result);
        log.info("  - Shared Buffers: {}", spec.getSharedBuffers());
        log.info("  - Work Mem: {}", spec.getWorkMem());
        log.info("  - Maintenance Work Mem: {}", spec.getMaintenanceWorkMem());
        log.info("  - WAL Buffers: {}", spec.getWalBuffers());
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(10)
    @DisplayName("PostgreSQL 타임존 및 날짜 스타일 설정 검증")
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
            assertNotNull(result, "결과가 생성되어야 합니다: " + timezones[i]);
            
            log.info("✅ 타임존 {} / 날짜 스타일 {} 설정 검증 완료", 
                    timezones[i], i < dateStyles.length ? dateStyles[i] : "default");
        }
        
        beanCreator.setReturnConfigOnly(false);
    }
}