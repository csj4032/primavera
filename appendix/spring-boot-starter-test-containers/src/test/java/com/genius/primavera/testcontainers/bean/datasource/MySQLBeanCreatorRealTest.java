package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.MySqlContainerSpec;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQL BeanCreator 실제 연결 테스트 (간단 버전)
 */
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
        
        // MySQL 컨테이너 생성
        container = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        
        container.start();
        
        // Spec 설정
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
        
        log.info("MySQL 테스트 컨테이너 시작됨: {}", container.getJdbcUrl());
    }

    @AfterAll
    void tearDown() {
        if (container != null) {
            container.stop();
            log.info("MySQL 테스트 컨테이너 중지됨");
        }
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
    @DisplayName("MySQL DataSource 빈 생성 및 실제 연결 테스트")
    void testCreateBeanWithRealConnection() {
        // ContainerInfo 생성
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql",
                ContainerType.MYSQL,
                container,
                spec
        );

        // 실제 DataSource 생성 및 연결 테스트
        HikariDataSource dataSource = (HikariDataSource) assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        
        assertNotNull(dataSource, "생성된 DataSource가 null이 아니어야 합니다");
        assertInstanceOf(HikariDataSource.class, dataSource, "HikariDataSource 인스턴스여야 합니다");
        
        // 실제 데이터베이스 연결 테스트
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(5), "연결이 유효해야 합니다");
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT 1");
                assertTrue(resultSet.next(), "쿼리 결과가 있어야 합니다");
                assertEquals(1, resultSet.getInt(1), "결과값이 1이어야 합니다");
                log.info("✅ MySQL 연결 테스트 성공");
            }
        }, "MySQL 연결이 성공해야 합니다");
        
        // DataSource 종료
        dataSource.close();
        log.info("✅ MySQL DataSource 생성 및 연결 테스트 성공");
    }

    @Test
    @Order(3)
    @DisplayName("MySQL SSL 비활성화 설정 테스트")
    void testSslDisabledSetting() {
        // SSL 비활성화 테스트
        spec.setSslEnabled(false);
        
        ContainerInfo containerInfo = new ContainerInfo(
                "test-mysql-ssl-disabled",
                ContainerType.MYSQL,
                container,
                spec
        );

        HikariDataSource dataSource = (HikariDataSource) beanCreator.createBean(containerInfo);
        assertNotNull(dataSource, "SSL 비활성화 DataSource가 생성되어야 합니다");
        
        // 실제 연결 테스트
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(3), "SSL 비활성화 연결이 유효해야 합니다");
            }
        });
        dataSource.close();
        log.info("✅ SSL 비활성화 설정 및 연결 검증 완료");
    }
}