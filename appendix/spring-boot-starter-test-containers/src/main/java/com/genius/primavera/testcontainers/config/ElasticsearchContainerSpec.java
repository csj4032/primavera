package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties
@EqualsAndHashCode(callSuper = true)
public class ElasticsearchContainerSpec extends BaseContainerSpec {

    @NotBlank(message = "Cluster name cannot be blank")
    private String clusterName = "elasticsearch-cluster";

    @NotBlank(message = "Node name cannot be blank")
    private String nodeName = "elasticsearch-node";

    @Pattern(regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+$", message = "Invalid Elasticsearch version format")
    private String esVersion = "8.13.4";

    private DiscoveryType discoveryType = DiscoveryType.SINGLE_NODE;

    private Boolean xpackSecurityEnabled = false;

    private Boolean xpackLicenseEnabled = false;

    private Boolean xpackMonitoringEnabled = false;

    @Size(min = 1, message = "HTTP port must be specified")
    private String httpPort = "9200";

    @Size(min = 1, message = "Transport port must be specified") 
    private String transportPort = "9300";

    @Min(value = 256, message = "Heap size must be at least 256m")
    private String heapSize = "1g";

    @NotNull
    private Map<@NotBlank String, @NotNull String> indexSettings = new HashMap<>();

    @NotNull
    private List<@NotBlank String> plugins = new ArrayList<>();

    private String networkHost = "0.0.0.0";

    private Integer httpMaxContentLength = 104857600; // 100MB

    private Boolean httpCompression = true;

    private Integer maxClauseCount = 1024;

    private String pathData = "/usr/share/elasticsearch/data";

    private String pathLogs = "/usr/share/elasticsearch/logs";

    public enum DiscoveryType {
        SINGLE_NODE,
        ZEN,
        EC2,
        GCE,
        AZURE
    }
}