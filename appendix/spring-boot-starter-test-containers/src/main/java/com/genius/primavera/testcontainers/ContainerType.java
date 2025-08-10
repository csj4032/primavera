package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import lombok.Getter;

@Getter
public enum ContainerType {
    MARIADB("mariadb:11.4.7", "org.mariadb.jdbc.Driver", MariaDbContainerSpec.class, true),
    MYSQL("mysql:8.0", "com.mysql.cj.jdbc.Driver", MySqlContainerSpec.class, true),
    POSTGRESQL("postgres:16", "org.postgresql.Driver", PostgreSqlContainerSpec.class, true),
    REDIS("redis:7-alpine", null, RedisContainerSpec.class, false),
    MONGODB("mongo:7", "com.mongodb.MongoClient", MongoContainerSpec.class, false),
    KAFKA("confluentinc/cp-kafka:7.5.0", null, BaseContainerSpec.class, false),  // TODO: KafkaContainerSpec
    ELASTICSEARCH("docker.elastic.co/elasticsearch/elasticsearch:8.12.0", null, BaseContainerSpec.class, false),  // TODO: ElasticsearchContainerSpec
    VAULT("hashicorp/vault:1.14.0", null, BaseContainerSpec.class, false),  // TODO: VaultContainerSpec
    LOCALSTACK("localstack/localstack:3.0", null, LocalStackContainerSpec.class, false);

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

    public boolean isSqlDatabase() {
        return isSqlDatabase;
    }

    public boolean isNoSqlDatabase() {
        return this == MONGODB;
    }

    public boolean isCacheSystem() {
        return this == REDIS;
    }

    public boolean isMessageQueue() {
        return this == KAFKA;
    }

    public boolean isSearchEngine() {
        return this == ELASTICSEARCH;
    }

    public boolean isSecretManagement() {
        return this == VAULT;
    }

    public boolean isAwsMockService() {
        return this == LOCALSTACK;
    }

    public String createJdbcUrl(String host, Integer port, String database) {
        if (!isSqlDatabase()) throw new UnsupportedOperationException("JDBC URL not supported for " + this);
        String dbName = database != null ? database : "test";
        return switch (this) {
            case MARIADB -> String.format("jdbc:mariadb://%s:%d/%s?characterEncoding=UTF-8&serverTimezone=Asia/Seoul", host, port, dbName);
            case MYSQL -> String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul", host, port, dbName);
            case POSTGRESQL -> String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
            default -> throw new UnsupportedOperationException("Unsupported database type: " + this);
        };
    }


    public String createMongoUri(String host, Integer port, String database, String username, String password, String authDatabase) {
        if (this != MONGODB) throw new UnsupportedOperationException("MongoDB URI not supported for " + this);
        StringBuilder uri = new StringBuilder("mongodb://");
        if (username != null && password != null) uri.append(username).append(":").append(password).append("@");
        uri.append(host).append(":").append(port);
        if (database != null) uri.append("/").append(database);
        if (authDatabase != null && !authDatabase.equals(database)) uri.append("?authSource=").append(authDatabase);
        return uri.toString();
    }

    public String createRedisUri(String host, Integer port, String password, Integer database) {
        if (this != REDIS) throw new UnsupportedOperationException("Redis URI not supported for " + this);
        StringBuilder uri = new StringBuilder("redis://");
        if (password != null) uri.append(":").append(password).append("@");
        uri.append(host).append(":").append(port);
        if (database != null && database > 0) uri.append("/").append(database);
        return uri.toString();
    }
}