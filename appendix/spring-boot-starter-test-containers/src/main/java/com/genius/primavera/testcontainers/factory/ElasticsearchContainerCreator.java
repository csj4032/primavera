package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class ElasticsearchContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.ELASTICSEARCH.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;
        
        ElasticsearchContainer container = new ElasticsearchContainer(DockerImageName.parse(image))
                .withStartupTimeout(Duration.ofSeconds(timeout))
                .withEnv("xpack.security.enabled", "false")
                .withEnv("xpack.security.http.ssl.enabled", "false")
                .withEnv("xpack.security.transport.ssl.enabled", "false");
        
        // 공통 환경 변수 적용
        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }
        
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.ELASTICSEARCH;
    }
}