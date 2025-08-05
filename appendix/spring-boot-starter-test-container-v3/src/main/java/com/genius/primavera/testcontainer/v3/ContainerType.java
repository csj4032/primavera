package com.genius.primavera.testcontainer.v3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지원하는 TestContainer 타입 (v3)
 */
@Getter
@RequiredArgsConstructor
public enum ContainerType {
    MARIADB("mariadb", "mariadb:11.4.7", 3306, "org.mariadb.jdbc.Driver"),
    MYSQL("mysql", "mysql:8.0", 3306, "com.mysql.cj.jdbc.Driver"),
    POSTGRESQL("postgresql", "postgres:15", 5432, "org.postgresql.Driver"),
    MONGODB("mongodb", "mongo:7.0", 27017, null),
    REDIS("redis", "redis:7-alpine", 6379, null),
    KAFKA("kafka", "confluentinc/cp-kafka:7.5.0", 9092, null),
    ELASTICSEARCH("elasticsearch", "docker.elastic.co/elasticsearch/elasticsearch:8.11.0", 9200, null);
    
    private final String name;
    private final String defaultImage;
    private final int defaultPort;
    private final String driverClassName;
    
    /**
     * SQL 데이터베이스 여부
     */
    public boolean isSqlDatabase() {
        return this == MARIADB || this == MYSQL || this == POSTGRESQL;
    }
    
    /**
     * NoSQL 데이터베이스 여부
     */
    public boolean isNoSqlDatabase() {
        return this == MONGODB || this == REDIS || this == ELASTICSEARCH;
    }
    
    /**
     * 메시징 시스템 여부
     */
    public boolean isMessaging() {
        return this == KAFKA;
    }
    
    /**
     * JDBC URL 패턴 생성
     */
    public String createJdbcUrl(String host, int port, String database) {
        if (!isSqlDatabase()) {
            throw new UnsupportedOperationException("JDBC URL is only supported for SQL databases");
        }
        
        switch (this) {
            case MARIADB:
                return String.format("jdbc:mariadb://%s:%d/%s", host, port, database);
            case MYSQL:
                return String.format("jdbc:mysql://%s:%d/%s", host, port, database);
            case POSTGRESQL:
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            default:
                throw new UnsupportedOperationException("Unsupported database type: " + this);
        }
    }
}