package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.testcontainers.containers.MariaDBContainer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("MariaDB 컨테이너 전략 테스트")
class MariaDBContainerStrategyTest {

    private MariaDBContainerStrategy strategy;
    private PrimaveraTestcontainersProperties.Mariadb config;
    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        environment = new MockEnvironment();
        config = new PrimaveraTestcontainersProperties.Mariadb();
        config.setImage("mariadb:11.4.7");
        config.setDatabaseName("test_db");
        config.setUsername("test_user");
        config.setPassword("test_pass");
        config.setInitScript("sql/test-init.sql");
        
        strategy = new MariaDBContainerStrategy(environment, config);
    }

    @Test
    @DisplayName("컨테이너 타입이 MARIADB인지 확인")
    void shouldReturnCorrectContainerType() {
        assertEquals(ContainerType.MARIADB, strategy.getContainerType());
    }

    @Test
    @DisplayName("컨테이너가 올바르게 생성되는지 확인")
    void shouldCreateContainerWithCorrectConfiguration() {
        MariaDBContainer<?> container = (MariaDBContainer<?>) strategy.getContainer();
        
        assertNotNull(container, "Container should not be null");
        assertEquals(config.getDatabaseName(), container.getDatabaseName());
        assertEquals(config.getUsername(), container.getUsername());
        assertEquals(config.getPassword(), container.getPassword());
        
        log.info("Container created successfully with database: {}", container.getDatabaseName());
    }

    @Test
    @DisplayName("Spring 프로퍼티가 올바르게 생성되는지 확인")
    void shouldGenerateCorrectSpringProperties() {
        MariaDBContainer<?> container = (MariaDBContainer<?>) strategy.getContainer();
        container.start();
        
        try {
            Map<String, Object> properties = strategy.getSpringProperties(container);
            
            assertTrue(properties.containsKey("spring.datasource.url"));
            assertTrue(properties.containsKey("spring.datasource.username"));
            assertTrue(properties.containsKey("spring.datasource.password"));
            assertTrue(properties.containsKey("spring.datasource.driver-class-name"));
            
            String jdbcUrl = (String) properties.get("spring.datasource.url");
            assertTrue(jdbcUrl.startsWith("jdbc:mariadb://"));
            assertTrue(jdbcUrl.contains("test_db"));
            
            assertEquals("test_user", properties.get("spring.datasource.username"));
            assertEquals("test_pass", properties.get("spring.datasource.password"));
            assertEquals("org.mariadb.jdbc.Driver", properties.get("spring.datasource.driver-class-name"));
            
            log.info("Generated properties: {}", properties);
        } finally {
            container.stop();
        }
    }

    @Test
    @DisplayName("초기 상태에서 컨테이너가 실행 중이지 않은지 확인")
    void shouldNotBeRunningInitially() {
        assertFalse(strategy.isRunning());
    }

    @Test
    @DisplayName("컨테이너 시작 후 실행 중인지 확인")
    void shouldBeRunningAfterStart() {
        MariaDBContainer<?> container = (MariaDBContainer<?>) strategy.getContainer();
        container.start();
        
        try {
            assertTrue(strategy.isRunning());
            assertTrue(container.isRunning());
            
            // 포트가 매핑되었는지 확인
            Integer mappedPort = container.getMappedPort(3306);
            assertNotNull(mappedPort);
            assertTrue(mappedPort > 0);
            
            log.info("Container started on port: {}", mappedPort);
        } finally {
            container.stop();
        }
    }
}