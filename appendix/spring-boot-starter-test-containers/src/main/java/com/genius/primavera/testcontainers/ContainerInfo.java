package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import org.testcontainers.containers.GenericContainer;

public record ContainerInfo(
        String name,
        ContainerType type,
        GenericContainer<?> container,
        BaseContainerSpec spec
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
        
        String database = getDatabaseName();
        return type.createJdbcUrl(getHost(), getMappedPort(), database);
    }

    public String getMongoUri() {
        if (type != ContainerType.MONGODB) {
            throw new UnsupportedOperationException("MongoDB URI only available for MongoDB");
        }
        
        if (spec instanceof MongoContainerSpec mongoSpec) {
            return type.createMongoUri(getHost(), getMappedPort(), 
                mongoSpec.getDatabase(), mongoSpec.getUsername(), 
                mongoSpec.getPassword(), mongoSpec.getAuthDatabase());
        }
        
        return type.createMongoUri(getHost(), getMappedPort(), "primavera", "primavera", "primavera", "admin");
    }

    public String getRedisUri() {
        if (type != ContainerType.REDIS) {
            throw new UnsupportedOperationException("Redis URI only available for Redis");
        }
        
        if (spec instanceof RedisContainerSpec redisSpec) {
            return type.createRedisUri(getHost(), getMappedPort(), 
                redisSpec.getPassword(), 0);
        }
        
        return type.createRedisUri(getHost(), getMappedPort(), null, 0);
    }

    public String getConnectionString() {
        return switch (type) {
            case MARIADB, MYSQL, POSTGRESQL -> getJdbcUrl();
            case MONGODB -> getMongoUri();
            case REDIS -> getRedisUri();
            case KAFKA -> String.format("%s:%d", getHost(), getMappedPort());
            case ELASTICSEARCH -> String.format("http://%s:%d", getHost(), getMappedPort());
            case VAULT -> String.format("http://%s:%d", getHost(), getMappedPort());
            case LOCALSTACK -> String.format("http://%s:%d", getHost(), getMappedPort());
        };
    }

    private String getDatabaseName() {
        return switch (type) {
            case MARIADB, MYSQL, POSTGRESQL -> {
                if (spec instanceof DatabaseContainerSpec dbSpec) {
                    yield dbSpec.getDatabase();
                }
                yield "primavera";
            }
            case MONGODB -> {
                if (spec instanceof MongoContainerSpec mongoSpec) {
                    yield mongoSpec.getDatabase();
                }
                yield "primavera";
            }
            default -> "primavera";
        };
    }
}