package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class ElasticsearchContainerStrategy implements ContainerStrategy {
    
    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        String imageName = config.getDockerImageName() != null ? config.getDockerImageName() : "docker.elastic.co/elasticsearch/elasticsearch:8.8.0";
        
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(imageName))
                .withExposedPorts(9200, 9300)
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false");
        
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            container.withEnv("ELASTIC_PASSWORD", config.getPassword());
            container.withEnv("xpack.security.enabled", "true");
        }
        
        // 환경 변수 설정
        config.getEnvironment().forEach(container::withEnv);
        
        return container;
    }
    
    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        String host = container.getHost();
        Integer port = container.getMappedPort(9200);
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "spring.elasticsearch.uris=http://" + host + ":" + port
        );
    }
    
    @Override
    public String getSupportedType() {
        return "elasticsearch";
    }
}