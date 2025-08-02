package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaContainerStrategy implements ContainerStrategy {
    
    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        String imageName = config.getDockerImageName() != null ? config.getDockerImageName() : "confluentinc/cp-kafka:7.4.0";
        
        KafkaContainer container = new KafkaContainer(DockerImageName.parse(imageName));
        
        // 환경 변수 설정
        config.getEnvironment().forEach(container::withEnv);
        
        return container;
    }
    
    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        KafkaContainer kafkaContainer = (KafkaContainer) container;
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "spring.kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers()
        );
    }
    
    @Override
    public String getSupportedType() {
        return "kafka";
    }
}