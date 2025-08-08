package com.genius.primavera.testcontainers;

import org.testcontainers.containers.GenericContainer;

public record ContainerInfo(
        String name,
        ContainerType type,
        GenericContainer<?> container,
        ContainerConfiguration.ContainerSpec spec
) {

    public ContainerInfo {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Container name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Container type cannot be null");
        }
        if (container == null) {
            throw new IllegalArgumentException("Container instance cannot be null");
        }
        if (spec == null) {
            throw new IllegalArgumentException("Container spec cannot be null");
        }
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
            case VAULT -> String.format("http://%s:%d", getHost(), getMappedPort());
        };
    }
}