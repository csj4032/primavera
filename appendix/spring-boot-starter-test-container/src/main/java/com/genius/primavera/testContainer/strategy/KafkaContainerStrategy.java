package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class KafkaContainerStrategy extends AbstractContainerStrategy<KafkaContainer> {

    private final PrimaveraTestcontainersProperties.Kafka config;

    public KafkaContainerStrategy(Environment environment, PrimaveraTestcontainersProperties.Kafka config) {
        super(ContainerType.KAFKA, environment);
        this.config = config;
    }

    @Override
    protected KafkaContainer createContainer() {
        KafkaContainer container = new KafkaContainer(DockerImageName.parse(config.getImage()));
        
        if (config.getStartupTimeout() != null) {
            container.withStartupTimeout(config.getStartupTimeout());
        }
        
        return container;
    }

    @Override
    public Map<String, Object> getSpringProperties(KafkaContainer container) {
        if (!container.isRunning()) {
            throw new IllegalStateException("Container must be started before accessing properties");
        }
        var properties = new HashMap<String, Object>();
        properties.put("spring.kafka.bootstrap-servers", container.getBootstrapServers());
        if (config.getAdditionalProperties() != null) {
            config.getAdditionalProperties().forEach((key, value) -> 
                properties.put("spring.kafka." + key, value)
            );
        }
        
        return properties;
    }
}