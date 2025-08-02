package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class ElasticsearchContainerStrategy implements ContainerStrategy {
    
    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        if (!(config instanceof PrimaveraTestcontainersProperties.ElasticsearchConfig)) {
            throw new IllegalArgumentException("Elasticsearch requires ElasticsearchConfig");
        }
        
        PrimaveraTestcontainersProperties.ElasticsearchConfig esConfig = 
            (PrimaveraTestcontainersProperties.ElasticsearchConfig) config;
            
        String imageName = esConfig.getDockerImageName() != null ? esConfig.getDockerImageName() : "docker.elastic.co/elasticsearch/elasticsearch:8.8.0";
        
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(imageName))
                .withExposedPorts(esConfig.getHttpPort(), esConfig.getTransportPort())
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false")
                .withEnv("cluster.name", esConfig.getClusterName());
        
        // 환경 변수 설정
        esConfig.getEnvironment().forEach(container::withEnv);
        
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