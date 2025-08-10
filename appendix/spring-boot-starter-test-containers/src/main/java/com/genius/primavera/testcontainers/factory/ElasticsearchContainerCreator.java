package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.ElasticsearchContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Slf4j
public class ElasticsearchContainerCreator implements ContainerCreator {

    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        log.info("Received spec type: {}", spec.getClass().getSimpleName());

        String image = spec.getImage() != null ? spec.getImage() : ContainerType.ELASTICSEARCH.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;

        ElasticsearchContainer container = new ElasticsearchContainer(DockerImageName.parse(image)).withStartupTimeout(Duration.ofSeconds(timeout));

        if (spec instanceof ElasticsearchContainerSpec esSpec) {
            log.info("Using ElasticsearchContainerSpec");
            log.info("ElasticsearchContainerSpec details:");
            log.info("  - clusterName: {}", esSpec.getClusterName());
            log.info("  - nodeName: {}", esSpec.getNodeName());
            log.info("  - discoveryType: {}", esSpec.getDiscoveryType());
            log.info("  - xpackSecurityEnabled: {}", esSpec.getXpackSecurityEnabled());
            log.info("  - heapSize: {}", esSpec.getHeapSize());

            container.withEnv("cluster.name", esSpec.getClusterName());
            container.withEnv("node.name", esSpec.getNodeName());

            if (esSpec.getDiscoveryType() == ElasticsearchContainerSpec.DiscoveryType.SINGLE_NODE) container.withEnv("discovery.type", "single-node");
            if (!esSpec.getXpackSecurityEnabled()) {
                container.withEnv("xpack.security.enabled", "false");
                container.withEnv("xpack.security.enrollment.enabled", "false");
                container.withEnv("xpack.security.http.ssl.enabled", "false");
                container.withEnv("xpack.security.transport.ssl.enabled", "false");
            }
            // Skip deprecated xpack.license.enabled and xpack.monitoring.enabled settings for ES 8.x
            if (esSpec.getNetworkHost() != null) container.withEnv("network.host", esSpec.getNetworkHost());
            if (esSpec.getHttpPort() != null) container.withEnv("http.port", esSpec.getHttpPort());
            if (esSpec.getTransportPort() != null) container.withEnv("transport.port", esSpec.getTransportPort());
            if (esSpec.getHttpMaxContentLength() != null) container.withEnv("http.max_content_length", esSpec.getHttpMaxContentLength() + "b");
            if (esSpec.getHttpCompression()) container.withEnv("http.compression", "true");
            if (esSpec.getHeapSize() != null) container.withEnv("ES_JAVA_OPTS", "-Xms" + esSpec.getHeapSize() + " -Xmx" + esSpec.getHeapSize());
            if (esSpec.getMaxClauseCount() != null) container.withEnv("indices.query.bool.max_clause_count", esSpec.getMaxClauseCount().toString());
            if (esSpec.getPathData() != null) container.withEnv("path.data", esSpec.getPathData());
            if (esSpec.getPathLogs() != null) container.withEnv("path.logs", esSpec.getPathLogs());
            esSpec.getIndexSettings().forEach((key, value) -> container.withEnv("indices.settings." + key, value));
        } else {
            log.info("Using default Elasticsearch configuration - spec type: {}", spec.getClass().getSimpleName());
            container.withEnv("discovery.type", "single-node");
            container.withEnv("xpack.security.enabled", "false");
            container.withEnv("xpack.security.enrollment.enabled", "false");
            container.withEnv("xpack.security.http.ssl.enabled", "false");
            container.withEnv("xpack.security.transport.ssl.enabled", "false");
        }
        if (spec.getEnvironment() != null) spec.getEnvironment().forEach(container::withEnv);
        return container;
    }

    @Override
    public ContainerType getSupportedType() {
        return ContainerType.ELASTICSEARCH;
    }
}