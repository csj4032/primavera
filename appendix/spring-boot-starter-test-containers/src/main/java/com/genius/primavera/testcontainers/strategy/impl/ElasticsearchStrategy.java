package com.genius.primavera.testcontainers.strategy.impl;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.ElasticsearchContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategy;
import com.genius.primavera.testcontainers.ContainerConfiguration;

import java.util.Map;

/**
 * Elasticsearch-specific strategy implementation
 */
public class ElasticsearchStrategy implements ContainerTypeStrategy {

    @Override
    public ContainerType getSupportedType() {
        return ContainerType.ELASTICSEARCH;
    }

    @Override
    public void applyDefaults(BaseContainerSpec spec) {
        // ElasticsearchContainerSpec defaults are handled in the spec itself
    }

    @Override
    public BaseContainerSpec getSpecFromConfiguration(Object config) {
        if (config instanceof ContainerConfiguration.ContainerInstanceConfig instanceConfig) return instanceConfig.getElasticsearch();
        return null;
    }

    @Override
    public void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        String esPrefix = "spring.elasticsearch." + containerInfo.name();
        properties.put(esPrefix + ".uris", containerInfo.getConnectionString());
    }

    @Override
    public BaseContainerSpec createDefaultSpec() {
        ElasticsearchContainerSpec spec = new ElasticsearchContainerSpec();
        spec.setImage(ContainerType.ELASTICSEARCH.getDefaultImage());
        applyDefaults(spec);
        return spec;
    }
}