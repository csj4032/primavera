package com.genius.primavera.test.strategy;

import com.genius.primavera.test.config.KafkaContainerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka TestContainer 관리 Strategy
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaContainerStrategy implements ContainerStrategy {
    
    private static final String CONTAINER_TYPE = "kafka";
    private final KafkaContainerConfig config;
    private ConfluentKafkaContainer container;
    
    @Override
    public GenericContainer<?> startContainer(ConfigurableApplicationContext context) {
        if (container == null) {
            container = createContainer();
        }
        
        if (!container.isRunning()) {
            log.info("Starting Kafka container with image: {}", config.getImage());
            container.start();
            log.info("Kafka container started successfully");
        }
        
        configureSpringProperties(context);
        return container;
    }
    
    @Override
    public String getContainerType() {
        return CONTAINER_TYPE;
    }
    
    @Override
    public boolean isRunning() {
        return container != null && container.isRunning();
    }
    
    @Override
    public GenericContainer<?> getContainer() {
        return container;
    }
    
    private ConfluentKafkaContainer createContainer() {
        var containerBuilder = new ConfluentKafkaContainer(DockerImageName.parse(config.getImage()));
        
        if (config.isReuse()) {
            containerBuilder.withReuse(true);
        }
        
        return containerBuilder;
    }
    
    private void configureSpringProperties(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        Map<String, Object> properties = new HashMap<>();
        
        properties.put("spring.kafka.bootstrap-servers", container.getBootstrapServers());
        
        environment.getPropertySources().addFirst(
            new MapPropertySource("testcontainers-kafka", properties)
        );
        
        log.debug("Kafka properties configured: bootstrap-servers={}", container.getBootstrapServers());
    }
}