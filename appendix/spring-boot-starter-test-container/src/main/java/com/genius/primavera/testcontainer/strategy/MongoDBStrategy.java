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
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(config.getDockerImageName())).withExposedPorts(mongoConfig.getPort());
        if (mongoConfig.getUsername() != null && !mongoConfig.getUsername().isEmpty()) container.withEnv("MONGO_INITDB_ROOT_USERNAME", mongoConfig.getUsername());
        if (mongoConfig.getPassword() != null && !mongoConfig.getPassword().isEmpty()) container.withEnv("MONGO_INITDB_ROOT_PASSWORD", mongoConfig.getPassword());
        if (mongoConfig.getDatabase() != null && !mongoConfig.getDatabase().isEmpty()) container.withEnv("MONGO_INITDB_DATABASE", mongoConfig.getDatabase());
        config.getEnvironment().forEach(container::withEnv);
        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        Map<String, Object> properties = new HashMap<>();
        String host = container.getHost();
        Integer port = container.getMappedPort(27017);
        String database = "test";
        String mongoUri;
        if (container.getEnvMap().containsKey("MONGO_INITDB_ROOT_USERNAME")) {
            String username = container.getEnvMap().get("MONGO_INITDB_ROOT_USERNAME");
            String password = container.getEnvMap().get("MONGO_INITDB_ROOT_PASSWORD");
            mongoUri = String.format("mongodb://%s:%s@%s:%d/%s?authSource=admin", username, password, host, port, database);
            properties.put("spring.data.mongodb.username", username);
            properties.put("spring.data.mongodb.password", password);
            properties.put("spring.data.mongodb.authentication-database", "admin");
            properties.put("spring.data.mongodb.reactive.username", username);
            properties.put("spring.data.mongodb.reactive.password", password);
            properties.put("spring.data.mongodb.reactive.authentication-database", "admin");
        } else {
            mongoUri = String.format("mongodb://%s:%d/%s", host, port, database);
        }

        properties.put("spring.data.mongodb.uri", mongoUri);
        properties.put("spring.data.mongodb.host", host);
        properties.put("spring.data.mongodb.port", port);
        properties.put("spring.data.mongodb.database", database);
        properties.put("spring.data.mongodb.reactive.uri", mongoUri);
        properties.put("spring.data.mongodb.reactive.host", host);
        properties.put("spring.data.mongodb.reactive.port", port);
        properties.put("spring.data.mongodb.reactive.database", database);
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-mongodb", properties));
    }

}
