package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import org.testcontainers.containers.GenericContainer;

/**
 * 시작된 컨테이너의 정보를 담는 레코드
 * 컨테이너의 연결 정보와 설정을 제공합니다.
 */
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

    /**
     * 컨테이너의 호스트 주소를 반환합니다.
     * 
     * @return 호스트 주소 (일반적으로 localhost)
     */
    public String getHost() {
        return container.getHost();
    }

    /**
     * 컨테이너의 매핑된 포트를 반환합니다.
     * 
     * @return 호스트에서 접근 가능한 포트 번호
     */
    public Integer getMappedPort() {
        return container.getFirstMappedPort();
    }

    /**
     * JDBC URL을 반환합니다 (SQL 데이터베이스만 지원).
     * 
     * @return JDBC 연결 URL
     * @throws UnsupportedOperationException SQL 데이터베이스가 아닌 경우
     */
    public String getJdbcUrl() {
        if (!type.isSqlDatabase()) {
            throw new UnsupportedOperationException("JDBC URL only available for SQL databases");
        }
        
        String database = getDatabaseName();
        return type.createJdbcUrl(getHost(), getMappedPort(), database);
    }

    /**
     * MongoDB 연결 URI를 반환합니다 (MongoDB만 지원).
     * 
     * @return MongoDB 연결 URI
     * @throws UnsupportedOperationException MongoDB가 아닌 경우
     */
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

    /**
     * Redis 연결 URI를 반환합니다 (Redis만 지원).
     * 
     * @return Redis 연결 URI
     * @throws UnsupportedOperationException Redis가 아닌 경우
     */
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

    /**
     * 범용 연결 문자열을 반환합니다.
     * 각 컨테이너 타입에 맞는 연결 정보를 제공합니다.
     * 
     * @return 연결 문자열
     */
    public String getConnectionString() {
        return switch (type) {
            case MARIADB, MYSQL, POSTGRESQL -> getJdbcUrl();
            case MONGODB -> getMongoUri();
            case REDIS -> getRedisUri();
            case KAFKA -> String.format("%s:%d", getHost(), getMappedPort());
            case ELASTICSEARCH -> String.format("http://%s:%d", getHost(), getMappedPort());
            case VAULT -> String.format("http://%s:%d", getHost(), getMappedPort());
        };
    }

    /**
     * 데이터베이스명을 반환합니다 (데이터베이스 타입만 지원).
     * 
     * @return 데이터베이스명
     */
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