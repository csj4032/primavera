package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import lombok.Getter;

/**
 * 지원되는 컨테이너 타입들과 각 타입의 기본 설정
 */
@Getter
public enum ContainerType {
    MARIADB("mariadb:11.4.7", "org.mariadb.jdbc.Driver", MariaDbContainerSpec.class, true),
    MYSQL("mysql:8.0", "com.mysql.cj.jdbc.Driver", MySqlContainerSpec.class, true),
    POSTGRESQL("postgres:16", "org.postgresql.Driver", PostgreSqlContainerSpec.class, true),
    REDIS("redis:7-alpine", null, RedisContainerSpec.class, false),
    MONGODB("mongo:7", "com.mongodb.MongoClient", MongoContainerSpec.class, false),
    KAFKA("confluentinc/cp-kafka:7.5.0", null, BaseContainerSpec.class, false),  // TODO: KafkaContainerSpec
    ELASTICSEARCH("docker.elastic.co/elasticsearch/elasticsearch:8.12.0", null, BaseContainerSpec.class, false),  // TODO: ElasticsearchContainerSpec
    VAULT("hashicorp/vault:1.14.0", null, BaseContainerSpec.class, false);  // TODO: VaultContainerSpec

    private final String defaultImage;
    private final String driverClassName;
    private final Class<? extends BaseContainerSpec> specClass;
    private final boolean isSqlDatabase;

    ContainerType(String defaultImage, String driverClassName, Class<? extends BaseContainerSpec> specClass, boolean isSqlDatabase) {
        this.defaultImage = defaultImage;
        this.driverClassName = driverClassName;
        this.specClass = specClass;
        this.isSqlDatabase = isSqlDatabase;
    }

    /**
     * SQL 데이터베이스 타입인지 확인
     * 
     * @return SQL 데이터베이스 여부
     */
    public boolean isSqlDatabase() {
        return isSqlDatabase;
    }

    /**
     * NoSQL 데이터베이스 타입인지 확인
     * 
     * @return NoSQL 데이터베이스 여부 (MongoDB 등)
     */
    public boolean isNoSqlDatabase() {
        return this == MONGODB;
    }

    /**
     * 캐시 시스템 타입인지 확인
     * 
     * @return 캐시 시스템 여부 (Redis 등)
     */
    public boolean isCacheSystem() {
        return this == REDIS;
    }

    /**
     * 메시지 큐 시스템 타입인지 확인
     * 
     * @return 메시지 큐 시스템 여부 (Kafka 등)
     */
    public boolean isMessageQueue() {
        return this == KAFKA;
    }

    /**
     * 검색 엔진 타입인지 확인
     * 
     * @return 검색 엔진 여부 (Elasticsearch 등)
     */
    public boolean isSearchEngine() {
        return this == ELASTICSEARCH;
    }

    /**
     * 보안 관리 시스템 타입인지 확인
     * 
     * @return 보안 관리 시스템 여부 (Vault 등)
     */
    public boolean isSecretManagement() {
        return this == VAULT;
    }

    /**
     * JDBC URL 생성 (SQL 데이터베이스만 지원)
     * 
     * @param host 호스트명
     * @param port 포트 번호
     * @param database 데이터베이스명
     * @return JDBC URL
     * @throws UnsupportedOperationException SQL 데이터베이스가 아닌 경우
     */
    public String createJdbcUrl(String host, Integer port, String database) {
        if (!isSqlDatabase()) {
            throw new UnsupportedOperationException("JDBC URL not supported for " + this);
        }

        String dbName = database != null ? database : "test";
        return switch (this) {
            case MARIADB -> String.format("jdbc:mariadb://%s:%d/%s?characterEncoding=UTF-8&serverTimezone=Asia/Seoul", host, port, dbName);
            case MYSQL -> String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul", host, port, dbName);
            case POSTGRESQL -> String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
            default -> throw new UnsupportedOperationException("Unsupported database type: " + this);
        };
    }

    /**
     * MongoDB 연결 URI 생성
     * 
     * @param host 호스트명
     * @param port 포트 번호
     * @param database 데이터베이스명
     * @param username 사용자명 (선택사항)
     * @param password 비밀번호 (선택사항)
     * @param authDatabase 인증 데이터베이스 (선택사항)
     * @return MongoDB URI
     * @throws UnsupportedOperationException MongoDB가 아닌 경우
     */
    public String createMongoUri(String host, Integer port, String database, String username, String password, String authDatabase) {
        if (this != MONGODB) {
            throw new UnsupportedOperationException("MongoDB URI not supported for " + this);
        }

        StringBuilder uri = new StringBuilder("mongodb://");
        
        if (username != null && password != null) {
            uri.append(username).append(":").append(password).append("@");
        }
        
        uri.append(host).append(":").append(port);
        
        if (database != null) {
            uri.append("/").append(database);
        }
        
        if (authDatabase != null && !authDatabase.equals(database)) {
            uri.append("?authSource=").append(authDatabase);
        }
        
        return uri.toString();
    }

    /**
     * Redis 연결 URI 생성
     * 
     * @param host 호스트명
     * @param port 포트 번호
     * @param password 비밀번호 (선택사항)
     * @param database 데이터베이스 번호 (0-15)
     * @return Redis URI
     * @throws UnsupportedOperationException Redis가 아닌 경우
     */
    public String createRedisUri(String host, Integer port, String password, Integer database) {
        if (this != REDIS) {
            throw new UnsupportedOperationException("Redis URI not supported for " + this);
        }

        StringBuilder uri = new StringBuilder("redis://");
        
        if (password != null) {
            uri.append(":").append(password).append("@");
        }
        
        uri.append(host).append(":").append(port);
        
        if (database != null && database > 0) {
            uri.append("/").append(database);
        }
        
        return uri.toString();
    }
}