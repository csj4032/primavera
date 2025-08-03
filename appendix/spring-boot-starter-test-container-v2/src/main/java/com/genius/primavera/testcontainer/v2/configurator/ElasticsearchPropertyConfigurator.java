package com.genius.primavera.testcontainer.v2.configurator;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

@Slf4j
public class ElasticsearchPropertyConfigurator implements PropertyConfigurator {
    
    @Override
    public void configureSpringProperties(GenericContainer<?> container) {
        ElasticsearchContainer elasticsearchContainer = (ElasticsearchContainer) container;
        String httpHostAddress = elasticsearchContainer.getHttpHostAddress();
        
        System.setProperty("spring.elasticsearch.uris", httpHostAddress);
        
        log.info("Set Elasticsearch properties - URI: {}", httpHostAddress);
    }
    
    @Override
    public boolean supports(Class<? extends GenericContainer<?>> containerClass) {
        return ElasticsearchContainer.class.isAssignableFrom(containerClass);
    }
}