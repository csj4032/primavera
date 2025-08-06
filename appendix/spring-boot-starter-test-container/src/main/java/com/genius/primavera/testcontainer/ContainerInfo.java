package com.genius.primavera.testcontainer;

import lombok.Getter;
import org.testcontainers.containers.GenericContainer;

@Getter
public class ContainerInfo {
    private final String name;
    private final ContainerType type;
    private final GenericContainer<?> container;
    private final ContainerConfiguration.ContainerSpec spec;
    
    public ContainerInfo(String name, ContainerType type, GenericContainer<?> container, ContainerConfiguration.ContainerSpec spec) {
        this.name = name;
        this.type = type;
        this.container = container;
        this.spec = spec;
    }
    
    public String getHost() {
        return container.getHost();
    }
    
    public Integer getMappedPort() {
        return container.getFirstMappedPort();
    }
    
    public String getJdbcUrl() {
        if (!type.isSqlDatabase()) {
            throw new UnsupportedOperationException("JDBC URL only available for SQL databases");
        }
        return type.createJdbcUrl(getHost(), getMappedPort(), spec.getDatabaseOrDefault());
    }
    
    public String getConnectionString() {
        return switch (type) {
            case MARIADB, MYSQL, POSTGRESQL -> getJdbcUrl();
            case MONGODB -> String.format("mongodb://%s:%d/%s", getHost(), getMappedPort(), spec.getDatabaseOrDefault());
            case REDIS -> String.format("redis://%s:%d", getHost(), getMappedPort());
            case KAFKA -> String.format("%s:%d", getHost(), getMappedPort());
            case ELASTICSEARCH -> String.format("http://%s:%d", getHost(), getMappedPort());
        };
    }
}