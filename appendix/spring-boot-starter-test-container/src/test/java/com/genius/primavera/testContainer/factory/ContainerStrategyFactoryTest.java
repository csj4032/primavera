package com.genius.primavera.testContainer.factory;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.strategy.ContainerStrategy;
import com.genius.primavera.testContainer.strategy.KafkaContainerStrategy;
import com.genius.primavera.testContainer.strategy.MariaDBContainerStrategy;
import com.genius.primavera.testContainer.strategy.PostgreSQLContainerStrategy;
import com.genius.primavera.testContainer.strategy.RedisContainerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Order(1)
@DisplayName("컨테이너 전략 팩토리 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContainerStrategyFactoryTest {

    private ContainerStrategyFactory factory;
    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        environment = new MockEnvironment();
        factory = new ContainerStrategyFactory(environment);
    }

    @Test
    @Order(1)
    @DisplayName("MariaDB 전략을 올바르게 생성하는지 확인")
    void shouldCreateMariaDBStrategy() {
        ContainerStrategy strategy = factory.getStrategy(ContainerType.MARIADB);
        
        assertNotNull(strategy);
        assertInstanceOf(MariaDBContainerStrategy.class, strategy);
        assertEquals(ContainerType.MARIADB, strategy.getContainerType());
        
        log.info("Created MariaDB strategy: {}", strategy.getClass().getSimpleName());
    }

    @Test
    @Order(2)
    @DisplayName("Redis 전략을 올바르게 생성하는지 확인")
    void shouldCreateRedisStrategy() {
        ContainerStrategy strategy = factory.getStrategy(ContainerType.REDIS);
        
        assertNotNull(strategy);
        assertInstanceOf(RedisContainerStrategy.class, strategy);
        assertEquals(ContainerType.REDIS, strategy.getContainerType());
        
        log.info("Created Redis strategy: {}", strategy.getClass().getSimpleName());
    }

    @Test
    @Order(3)
    @DisplayName("Kafka 전략을 올바르게 생성하는지 확인")
    void shouldCreateKafkaStrategy() {
        ContainerStrategy strategy = factory.getStrategy(ContainerType.KAFKA);
        
        assertNotNull(strategy);
        assertInstanceOf(KafkaContainerStrategy.class, strategy);
        assertEquals(ContainerType.KAFKA, strategy.getContainerType());
        
        log.info("Created Kafka strategy: {}", strategy.getClass().getSimpleName());
    }

    @Test
    @Order(4)
    @DisplayName("PostgreSQL 전략을 올바르게 생성하는지 확인")
    void shouldCreatePostgreSQLStrategy() {
        ContainerStrategy strategy = factory.getStrategy(ContainerType.POSTGRESQL);
        
        assertNotNull(strategy);
        assertInstanceOf(PostgreSQLContainerStrategy.class, strategy);
        assertEquals(ContainerType.POSTGRESQL, strategy.getContainerType());
        
        log.info("Created PostgreSQL strategy: {}", strategy.getClass().getSimpleName());
    }

    @Test
    @Order(5)
    @DisplayName("모든 컨테이너 타입에 대해 전략을 생성할 수 있는지 확인")
    void shouldCreateStrategyForAllContainerTypes() {
        for (ContainerType containerType : ContainerType.values()) {
            ContainerStrategy strategy = factory.getStrategy(containerType);
            
            assertNotNull(strategy, "Strategy should not be null for " + containerType);
            assertEquals(containerType, strategy.getContainerType(), 
                    "Container type should match for " + containerType);
            
            log.info("Successfully created strategy for {}: {}", 
                    containerType, strategy.getClass().getSimpleName());
        }
    }

    @Test
    @Order(6)
    @DisplayName("커스텀 프로퍼티가 있는 환경에서 전략 생성 테스트")
    void shouldCreateStrategyWithCustomProperties() {
        // 커스텀 프로퍼티 설정
        environment.setProperty("primavera.testcontainers.mariadb.image", "mariadb:11.4.7");
        environment.setProperty("primavera.testcontainers.mariadb.database-name", "custom_db");
        environment.setProperty("primavera.testcontainers.redis.image", "redis:7-alpine");
        environment.setProperty("primavera.testcontainers.redis.port", "6380");
        
        ContainerStrategyFactory customFactory = new ContainerStrategyFactory(environment);
        
        // MariaDB 전략 테스트
        ContainerStrategy mariadbStrategy = customFactory.getStrategy(ContainerType.MARIADB);
        assertNotNull(mariadbStrategy);
        assertInstanceOf(MariaDBContainerStrategy.class, mariadbStrategy);
        
        // Redis 전략 테스트
        ContainerStrategy redisStrategy = customFactory.getStrategy(ContainerType.REDIS);
        assertNotNull(redisStrategy);
        assertInstanceOf(RedisContainerStrategy.class, redisStrategy);
        
        log.info("Successfully created strategies with custom properties");
    }

    @Test
    @Order(7)
    @DisplayName("동일한 컨테이너 타입에 대해 동일한 설정의 전략이 생성되는지 확인")
    void shouldCreateConsistentStrategies() {
        ContainerStrategy strategy1 = factory.getStrategy(ContainerType.MARIADB);
        ContainerStrategy strategy2 = factory.getStrategy(ContainerType.MARIADB);
        
        assertNotNull(strategy1);
        assertNotNull(strategy2);
        
        // 서로 다른 인스턴스이지만 동일한 타입과 설정을 가져야 함
        assertNotSame(strategy1, strategy2);
        assertEquals(strategy1.getContainerType(), strategy2.getContainerType());
        assertEquals(strategy1.getClass(), strategy2.getClass());
        
        log.info("Created consistent strategies: {} and {}", 
                strategy1.getClass().getSimpleName(),
                strategy2.getClass().getSimpleName());
    }
}