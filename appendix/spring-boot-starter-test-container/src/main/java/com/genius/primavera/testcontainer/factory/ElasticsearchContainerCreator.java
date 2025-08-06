package com.genius.primavera.testcontainer.factory;

import com.genius.primavera.testcontainer.ContainerConfiguration;
import com.genius.primavera.testcontainer.ContainerCreator;
import com.genius.primavera.testcontainer.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class ElasticsearchContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec) {
        ElasticsearchContainer container = new ElasticsearchContainer(DockerImageName.parse(spec.getImageOrDefault(ContainerType.ELASTICSEARCH)))
                .withStartupTimeout(Duration.ofSeconds(spec.getStartupTimeoutOrDefault()));
        
        ContainerConfigurationHelper.configureContainer(container, spec);
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.ELASTICSEARCH;
    }
}