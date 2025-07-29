package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.config.*;
import com.genius.primavera.testContainer.factory.ContainerStrategyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ContainerStrategy 아키텍처 테스트
 */
class ContainerStrategyTest {

    @Test
    @DisplayName("Factory에서 올바른 Strategy를 반환한다")
    void factoryReturnsCorrectStrategy() {
        ContainerStrategyFactory factory = createFactory();
        
        ContainerStrategy mariadbStrategy = factory.getStrategy(ContainerType.MARIADB);
        ContainerStrategy redisStrategy = factory.getStrategy(ContainerType.REDIS);
        ContainerStrategy kafkaStrategy = factory.getStrategy(ContainerType.KAFKA);
        ContainerStrategy postgresqlStrategy = factory.getStrategy(ContainerType.POSTGRESQL);
        
        assertThat(mariadbStrategy).isInstanceOf(MariaDBContainerStrategy.class);
        assertThat(redisStrategy).isInstanceOf(RedisContainerStrategy.class);
        assertThat(kafkaStrategy).isInstanceOf(KafkaContainerStrategy.class);
        assertThat(postgresqlStrategy).isInstanceOf(PostgreSQLContainerStrategy.class);
        
        assertThat(mariadbStrategy.getContainerType()).isEqualTo("mariadb");
        assertThat(redisStrategy.getContainerType()).isEqualTo("redis");
        assertThat(kafkaStrategy.getContainerType()).isEqualTo("kafka");
        assertThat(postgresqlStrategy.getContainerType()).isEqualTo("postgresql");
    }
    
    @Test
    @DisplayName("MariaDB Strategy가 올바른 컨테이너를 생성한다")
    void mariadbStrategyCreatesCorrectContainer() {
        ContainerStrategyFactory factory = createFactory();
        ContainerStrategy strategy = factory.getStrategy(ContainerType.MARIADB);
        GenericApplicationContext context = new GenericApplicationContext();
        
        assertThat(strategy.isRunning()).isFalse();
        assertThat(strategy.getContainer()).isNull();
        
        GenericContainer<?> container = strategy.startContainer(context);
        
        assertThat(container).isInstanceOf(MariaDBContainer.class);
        assertThat(strategy.isRunning()).isTrue();
        assertThat(strategy.getContainer()).isSameAs(container);
        
        // Cleanup
        container.stop();
    }
    
    @Test
    @DisplayName("지원하지 않는 컨테이너 타입에 대해 예외를 발생시킨다")
    void factoryThrowsExceptionForUnsupportedType() {
        ContainerStrategyFactory factory = createFactory();
        
        // ContainerType enum에 없는 값은 컴파일 타임에 체크되므로
        // 실제로는 null 체크나 다른 예외 상황을 테스트
        assertThrows(IllegalArgumentException.class, () -> {
            // 이는 실제로는 발생하지 않지만, null safety를 위한 테스트
            ContainerType nullType = null;
            factory.getStrategy(nullType);
        });
    }
    
    @Test
    @DisplayName("각 Strategy는 고유한 컨테이너 타입을 가진다")
    void eachStrategyHasUniqueContainerType() {
        ContainerStrategyFactory factory = createFactory();
        
        String mariadbType = factory.getStrategy(ContainerType.MARIADB).getContainerType();
        String redisType = factory.getStrategy(ContainerType.REDIS).getContainerType();
        String kafkaType = factory.getStrategy(ContainerType.KAFKA).getContainerType();
        String postgresqlType = factory.getStrategy(ContainerType.POSTGRESQL).getContainerType();
        
        assertThat(mariadbType).isNotEqualTo(redisType);
        assertThat(mariadbType).isNotEqualTo(kafkaType);
        assertThat(mariadbType).isNotEqualTo(postgresqlType);
        assertThat(redisType).isNotEqualTo(kafkaType);
        assertThat(redisType).isNotEqualTo(postgresqlType);
        assertThat(kafkaType).isNotEqualTo(postgresqlType);
    }
    
    private ContainerStrategyFactory createFactory() {
        return new ContainerStrategyFactory(
            new MariaDBContainerConfig(),
            new RedisContainerConfig(),
            new KafkaContainerConfig(),
            new PostgreSQLContainerConfig()
        );
    }
}