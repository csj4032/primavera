package com.genius.primavera.testcontainers.v4.examples;

import com.genius.primavera.testcontainers.v4.ContainerConfiguration;
import com.genius.primavera.testcontainers.v4.ContainerCreator;
import com.genius.primavera.testcontainers.v4.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * External plugin example: ClickHouse container creator
 * 
 * This demonstrates how to create an external plugin for spring-boot-starter-test-container-v4
 * 
 * To use this plugin:
 * 1. Add this class to your project
 * 2. Add the following line to META-INF/services/com.genius.primavera.testcontainers.v4.ContainerCreator:
 *    com.genius.primavera.testcontainers.v4.examples.ClickHouseContainerCreator
 * 3. Add CLICKHOUSE to your ContainerType enum (if extending)
 * 4. Use @EnableTestContainers with type = ContainerType.CLICKHOUSE
 * 
 * Example usage:
 * 
 * @EnableTestContainers({
 *     @EnableTestContainers.TestContainer(type = ContainerType.CLICKHOUSE, name = "analytics")
 * })
 * class AnalyticsTest {
 *     @Autowired
 *     @Qualifier("analytics")
 *     private DataSource clickHouseDataSource;
 * }
 * 
 * application-test.yml:
 * testcontainer:
 *   containers:
 *     analytics:
 *       image: "clickhouse/clickhouse-server:latest"
 *       database: "analytics_test"
 *       username: "test"
 *       password: "test"
 *       environment:
 *         CLICKHOUSE_DB: "analytics_test"
 *         CLICKHOUSE_USER: "test"
 *         CLICKHOUSE_PASSWORD: "test"
 */
public class ClickHouseContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec) {
        GenericContainer<?> container = new GenericContainer<>(
            DockerImageName.parse(spec.getImageOrDefault("clickhouse/clickhouse-server:latest")))
            .withExposedPorts(8123, 9000)  // HTTP and Native protocol ports
            .withStartupTimeout(Duration.ofSeconds(spec.getStartupTimeoutOrDefault()));
        
        // Configure ClickHouse specific environment variables
        container.withEnv("CLICKHOUSE_DB", spec.getDatabaseOrDefault());
        container.withEnv("CLICKHOUSE_USER", spec.getUsernameOrDefault());
        container.withEnv("CLICKHOUSE_PASSWORD", spec.getPasswordOrDefault());
        
        // Apply common container configuration
        configureContainer(container, spec);
        
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        // Note: You would need to extend ContainerType enum to include CLICKHOUSE
        // or use a custom enum implementation
        return ContainerType.valueOf("CLICKHOUSE");
    }
    
    private void configureContainer(GenericContainer<?> container, ContainerConfiguration.ContainerSpec spec) {
        if (spec.getEnvironment() != null) {
            container.withEnv(spec.getEnvironment());
        }
        
        if (spec.getNetworkAliases() != null) {
            for (String alias : spec.getNetworkAliases()) {
                container.withNetworkAliases(alias);
            }
        }
    }
}