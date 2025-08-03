package com.genius.primavera.testcontainer.v2.factory;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class ElasticsearchContainerFactory implements ContainerBuilderFactory {
    
    @Override
    public GenericContainer<?> createContainer(TestContainerProperties.ContainerConfig config) {
        TestContainerProperties.ElasticsearchConfig elasticsearchConfig = (TestContainerProperties.ElasticsearchConfig) config;
        
        ElasticsearchContainer container = new ElasticsearchContainer(DockerImageName.parse(elasticsearchConfig.getDockerImageName()))
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false");
        
        log.info("Created Elasticsearch container with image: {}", elasticsearchConfig.getDockerImageName());
        return container;
    }
    
    @Override
    public boolean supports(ContainerType containerType) {
        return containerType == ContainerType.ELASTICSEARCH;
    }
}