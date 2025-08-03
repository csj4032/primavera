package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class MongoDBStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        PrimaveraTestcontainersProperties.MongoDBConfig mongoConfig = (PrimaveraTestcontainersProperties.MongoDBConfig) config;
        
        // 일반적인 GenericContainer를 사용하여 MongoDB 컨테이너 생성 (레플리카셋 없이)
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(config.getDockerImageName()))
                .withExposedPorts(27017);
        
        // 인증 설정 (선택적)
        if (mongoConfig.getUsername() != null && !mongoConfig.getUsername().isEmpty()) {
            container.withEnv("MONGO_INITDB_ROOT_USERNAME", mongoConfig.getUsername());
        }
        if (mongoConfig.getPassword() != null && !mongoConfig.getPassword().isEmpty()) {
            container.withEnv("MONGO_INITDB_ROOT_PASSWORD", mongoConfig.getPassword());
        }
        if (mongoConfig.getDatabase() != null && !mongoConfig.getDatabase().isEmpty()) {
            container.withEnv("MONGO_INITDB_DATABASE", mongoConfig.getDatabase());
        }
        
        // 추가 환경 변수 설정
        config.getEnvironment().forEach(container::withEnv);
        
        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        Map<String, Object> properties = new HashMap<>();
        
        // 단순한 MongoDB URI 생성 (레플리카셋 없이)
        String host = container.getHost();
        Integer port = container.getMappedPort(27017);
        String database = "test"; // 기본 데이터베이스
        
        // 인증이 있는 경우와 없는 경우 구분
        String mongoUri;
        if (container.getEnvMap().containsKey("MONGO_INITDB_ROOT_USERNAME")) {
            String username = container.getEnvMap().get("MONGO_INITDB_ROOT_USERNAME");
            String password = container.getEnvMap().get("MONGO_INITDB_ROOT_PASSWORD");
            mongoUri = String.format("mongodb://%s:%s@%s:%d/%s?authSource=admin", 
                username, password, host, port, database);
            
            // 개별 인증 속성도 설정 (전통적 MongoDB)
            properties.put("spring.data.mongodb.username", username);
            properties.put("spring.data.mongodb.password", password);
            properties.put("spring.data.mongodb.authentication-database", "admin");
            
            // Reactive MongoDB 인증 설정
            properties.put("spring.data.mongodb.reactive.username", username);
            properties.put("spring.data.mongodb.reactive.password", password);
            properties.put("spring.data.mongodb.reactive.authentication-database", "admin");
        } else {
            // 인증 없는 경우
            mongoUri = String.format("mongodb://%s:%d/%s", host, port, database);
        }
        
        // MongoDB 연결 설정 (reactive 지원)
        properties.put("spring.data.mongodb.uri", mongoUri);
        properties.put("spring.data.mongodb.host", host);
        properties.put("spring.data.mongodb.port", port);
        properties.put("spring.data.mongodb.database", database);
        
        // Reactive MongoDB 설정
        properties.put("spring.data.mongodb.reactive.uri", mongoUri);
        properties.put("spring.data.mongodb.reactive.host", host);
        properties.put("spring.data.mongodb.reactive.port", port);
        properties.put("spring.data.mongodb.reactive.database", database);
        
        // Spring 환경에 속성 추가
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-mongodb", properties));
    }

}
