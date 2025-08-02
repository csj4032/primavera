package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class RedisContainerStrategy implements ContainerStrategy {
    
    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        if (!(config instanceof PrimaveraTestcontainersProperties.RedisConfig)) {
            throw new IllegalArgumentException("Redis requires RedisConfig");
        }
        
        PrimaveraTestcontainersProperties.RedisConfig redisConfig = 
            (PrimaveraTestcontainersProperties.RedisConfig) config;
            
        String imageName = redisConfig.getDockerImageName() != null ? redisConfig.getDockerImageName() : "redis:7-alpine";
        
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(imageName))
                .withExposedPorts(6379);
        
        // Redis 비밀번호 설정
        if (redisConfig.getPassword() != null && !redisConfig.getPassword().isEmpty()) {
            container.withCommand("redis-server", "--requirepass", redisConfig.getPassword());
        }
        
        // 환경 변수 설정
        redisConfig.getEnvironment().forEach(container::withEnv);
        
        return container;
    }
    
    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        String host = container.getHost();
        Integer port = container.getMappedPort(6379);
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "spring.data.redis.host=" + host,
                "spring.data.redis.port=" + port
        );
    }
    
    @Override
    public String getSupportedType() {
        return "redis";
    }
}