package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class KafkaContainerStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        KafkaContainer container = new KafkaContainer(DockerImageName.parse(config.getDockerImageName()));

        // 환경 변수 설정
        config.getEnvironment().forEach(container::withEnv);

        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        KafkaContainer kafkaContainer = (KafkaContainer) container;
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-kafka", properties));
    }
}