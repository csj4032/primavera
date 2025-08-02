package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.HashMap;
import java.util.Map;

public class ElasticsearchContainerStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        PrimaveraTestcontainersProperties.ElasticsearchConfig esConfig = (PrimaveraTestcontainersProperties.ElasticsearchConfig) config;
        
        ElasticsearchContainer container = new ElasticsearchContainer(config.getDockerImageName())
                .withEnv("cluster.name", esConfig.getClusterName())
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false");

        // 환경 변수 설정
        config.getEnvironment().forEach(container::withEnv);

        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        ElasticsearchContainer esContainer = (ElasticsearchContainer) container;
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.elasticsearch.uris", esContainer.getHttpHostAddress());

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-elasticsearch", properties));
    }
}