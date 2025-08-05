package com.genius.primavera.testcontainer.v4.factory;

import com.genius.primavera.testcontainer.v4.ContainerConfiguration;
import com.genius.primavera.testcontainer.v4.ContainerCreator;
import com.genius.primavera.testcontainer.v4.ContainerType;
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