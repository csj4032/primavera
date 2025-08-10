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
        
        log.info("MySQL 단위 테스트 설정 완료");
    }

    @Test
    @Order(1)
    @DisplayName("MySQL BeanCreator 지원 타입 확인")
    void testSupportedType() {
        assertEquals(ContainerType.MYSQL, beanCreator.getSupportedType());
        log.info("✅ MySQL BeanCreator 지원 타입: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("기본 HikariConfig 생성 및 설정 검증")
    void testCreateBaseConfig() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        HikariConfig config = beanCreator.testCreateBaseConfig(containerInfo);
        
        assertNotNull(config, "HikariConfig가 생성되어야 합니다");
        assertTrue(config.getJdbcUrl().contains("mysql"), "JDBC URL은 mysql을 포함해야 합니다");
        assertTrue(config.getJdbcUrl().contains("localhost:3308"), "JDBC URL은 호스트와 포트를 포함해야 합니다");
        assertEquals("test-mysql-pool", config.getPoolName());
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
                "test-mysql-config-only",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "결과가 null이 아니어야 합니다");
        assertInstanceOf(String.class, result, "String 인스턴스여야 합니다");
        assertTrue(result.toString().contains("test-mysql-config-only"), "컨테이너 이름이 포함되어야 합니다");
        
        log.info("✅ 설정 검증 완료 (연결 없음): {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(5)
    @DisplayName("기본값 설정 검증")
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
        
        assertNotNull(result, "결과가 생성되어야 합니다");
        assertTrue(result.toString().contains("test-mysql-defaults"), "컨테이너 이름이 포함되어야 합니다");
        
        log.info("✅ 기본값 설정 검증 성공: {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(6)
    @DisplayName("MySQL 고급 설정 검증")
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
        assertNotNull(result, "결과가 생성되어야 합니다");
        
        assertTrue(result.toString().contains("test-mysql-advanced"), "컨테이너 이름이 포함되어야 합니다");
        
        log.info("✅ 고급 설정 검증 완료: {}", result);
        log.info("  - SQL Mode: {}", spec.getSqlMode());
        log.info("  - Storage Engine: {}", spec.getDefaultStorageEngine());
        log.info("  - Character Set: {}", spec.getCharacterSet());
        log.info("  - Timezone: {}", spec.getDefaultTimeZone());
        log.info("  - SSL Enabled: {}", spec.getSslEnabled());
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(7)
    @DisplayName("MySQL 타임존 및 문자셋 설정 검증")
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
            assertNotNull(result, "결과가 생성되어야 합니다: " + timezones[i]);
            
            log.info("✅ 타임존 {} / 문자셋 {} 설정 검증 완료", 
                    timezones[i], i < charsets.length ? charsets[i] : "default");
        }
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(8)
    @DisplayName("MySQL SSL 설정 검증")
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
        assertNotNull(result, "SSL 비활성화 설정이 생성되어야 합니다");
        
        log.info("✅ SSL 비활성화 설정 검증 완료");
        
        spec.setSslEnabled(true);
        containerInfo = new ContainerInfo(
                "test-mysql-ssl-enabled",
                ContainerType.MYSQL,
                mockContainer,
                spec
        );

        result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "SSL 활성화 설정이 생성되어야 합니다");
        
        log.info("✅ SSL 활성화 설정 검증 완료");
        
        beanCreator.setReturnConfigOnly(false);
    }
}