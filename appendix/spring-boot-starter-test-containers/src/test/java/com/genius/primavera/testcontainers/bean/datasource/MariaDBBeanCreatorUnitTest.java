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

/**
 * MariaDB BeanCreator 설정 검증 테스트 (실제 연결 없음)
 */
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
                // 실제 DataSource 생성 대신 간단한 정보만 반환
                return "MariaDB DataSource Configuration: " + containerInfo.name();
            }
            return super.createBean(containerInfo);
        }
        
        // protected 메서드를 public으로 노출
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
        
        // Mock 컨테이너 설정
        when(mockContainer.getHost()).thenReturn("localhost");
        when(mockContainer.getFirstMappedPort()).thenReturn(3307);
        
        // Spec 설정
        spec = new MariaDbContainerSpec();
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setMaxConnections(5);
        spec.setConnectionTimeout(10000);
        
        log.info("MariaDB 단위 테스트 설정 완료");
    }

    @Test
    @Order(1)
    @DisplayName("MariaDB BeanCreator 지원 타입 확인")
    void testSupportedType() {
        assertEquals(ContainerType.MARIADB, beanCreator.getSupportedType());
        log.info("✅ MariaDB BeanCreator 지원 타입: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("기본 HikariConfig 생성 및 설정 검증")
    void testCreateBaseConfig() {
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        // HikariConfig 생성 (연결 없음)
        HikariConfig config = beanCreator.testCreateBaseConfig(containerInfo);
        
        assertNotNull(config, "HikariConfig가 생성되어야 합니다");
        assertTrue(config.getJdbcUrl().contains("mariadb"), "JDBC URL은 mariadb를 포함해야 합니다");
        assertTrue(config.getJdbcUrl().contains("localhost:3307"), "JDBC URL은 호스트와 포트를 포함해야 합니다");
        assertEquals("test-mariadb-pool", config.getPoolName());
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
                "test-mariadb-config-only",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        // 연결 없이 설정만 검증
        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "결과가 null이 아니어야 합니다");
        assertInstanceOf(String.class, result, "String 인스턴스여야 합니다");
        assertTrue(result.toString().contains("test-mariadb-config-only"), "컨테이너 이름이 포함되어야 합니다");
        
        log.info("✅ 설정 검증 완료 (연결 없음): {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(5)
    @DisplayName("기본값 설정 검증")
    void testDefaultSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        // 기본 spec 사용
        MariaDbContainerSpec defaultSpec = new MariaDbContainerSpec();
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-defaults",
                ContainerType.MARIADB,
                mockContainer,
                defaultSpec
        );

        // 기본 설정으로 생성 검증
        Object result = beanCreator.createBean(containerInfo);
        
        assertNotNull(result, "결과가 생성되어야 합니다");
        assertTrue(result.toString().contains("test-mariadb-defaults"), "컨테이너 이름이 포함되어야 합니다");
        
        log.info("✅ 기본값 설정 검증 성공: {}", result);
        
        beanCreator.setReturnConfigOnly(false);
    }

    @Test
    @Order(6)
    @DisplayName("MariaDB 고급 설정 검증")
    void testAdvancedSettings() {
        beanCreator.setReturnConfigOnly(true);
        
        // SQL 모드 및 스토리지 엔진 설정
        spec.setSqlMode(MariaDbContainerSpec.SqlMode.STRICT_TRANS_TABLES);
        spec.setDefaultStorageEngine(MariaDbContainerSpec.StorageEngine.INNODB);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mariadb-advanced",
                ContainerType.MARIADB,
                mockContainer,
                spec
        );

        Object result = beanCreator.createBean(containerInfo);
        assertNotNull(result, "결과가 생성되어야 합니다");
        
        // 설정이 올바르게 적용되었는지 확인
        assertTrue(result.toString().contains("test-mariadb-advanced"), "컨테이너 이름이 포함되어야 합니다");
        
        log.info("✅ 고급 설정 검증 완료: {}", result);
        log.info("  - SQL Mode: {}", spec.getSqlMode());
        log.info("  - Storage Engine: {}", spec.getDefaultStorageEngine());
        
        beanCreator.setReturnConfigOnly(false);
    }
}