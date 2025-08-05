package com.genius.primavera.testcontainer;

/**
 * 지원되는 TestContainer 타입들
 */
public enum ContainerType {
    /**
     * MariaDB 데이터베이스 컨테이너
     */
    MARIADB("mariadb", "mariadb:11.4.7", 3306),
    
    /**
     * MySQL 데이터베이스 컨테이너
     */
    MYSQL("mysql", "mysql:8.0", 3306),
    
    /**
     * PostgreSQL 데이터베이스 컨테이너
     */
    POSTGRESQL("postgresql", "postgres:15", 5432),
    
    /**
     * Redis 캐시 컨테이너
     */
    REDIS("redis", "redis:7-alpine", 6379),
    
    /**
     * MongoDB 문서 데이터베이스 컨테이너
     */
    MONGODB("mongodb", "mongo:7", 27017),
    
    /**
     * Elasticsearch 검색 엔진 컨테이너
     */
    ELASTICSEARCH("elasticsearch", "elasticsearch:8.11.0", 9200),
    
    /**
     * Apache Kafka 메시지 브로커 컨테이너
     */
    KAFKA("kafka", "confluentinc/cp-kafka:latest", 9092);

    private final String name;
    private final String defaultImage;
    private final int defaultPort;

    ContainerType(String name, String defaultImage, int defaultPort) {
        this.name = name;
        this.defaultImage = defaultImage;
        this.defaultPort = defaultPort;
    }

    public String getName() {
        return name;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * 데이터베이스 타입인지 확인
     */
    public boolean isDatabase() {
        return this == MARIADB || this == MYSQL || this == POSTGRESQL || this == MONGODB;
    }

    /**
     * 관계형 데이터베이스 타입인지 확인
     */
    public boolean isRelationalDatabase() {
        return this == MARIADB || this == MYSQL || this == POSTGRESQL;
    }

    /**
     * NoSQL 데이터베이스 타입인지 확인
     */
    public boolean isNoSqlDatabase() {
        return this == MONGODB;
    }

    /**
     * 캐시 타입인지 확인
     */
    public boolean isCache() {
        return this == REDIS;
    }

    /**
     * 메시지 브로커 타입인지 확인
     */
    public boolean isMessageBroker() {
        return this == KAFKA;
    }

    /**
     * 검색 엔진 타입인지 확인
     */
    public boolean isSearchEngine() {
        return this == ELASTICSEARCH;
    }
}