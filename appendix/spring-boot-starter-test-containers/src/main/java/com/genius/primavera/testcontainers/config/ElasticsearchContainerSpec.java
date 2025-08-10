package com.genius.primavera.testcontainers.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "primavera.testcontainers.elasticsearch")
public class ElasticsearchContainerSpec extends BaseContainerSpec {
    
    private String dockerImageName = "docker.elastic.co/elasticsearch/elasticsearch:8.11.1";
    private boolean securityEnabled = false;
    private String clusterName = "test-cluster";
    private String nodeName = "test-node";
    
    public ElasticsearchContainerSpec() {
        setImage(dockerImageName);
        if (!securityEnabled) {
            getEnvironment().put("xpack.security.enabled", "false");
            getEnvironment().put("xpack.security.http.ssl.enabled", "false");
            getEnvironment().put("discovery.type", "single-node");
            getEnvironment().put("cluster.name", clusterName);
            getEnvironment().put("node.name", nodeName);
        }
    }
}