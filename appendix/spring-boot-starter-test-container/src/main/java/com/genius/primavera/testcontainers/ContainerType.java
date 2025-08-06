package com.genius.primavera.testcontainers;

import lombok.Getter;

@Getter
public enum ContainerType {
    MARIADB("mariadb:11.4.7", "org.mariadb.jdbc.Driver"),
    MYSQL("mysql:8.0", "com.mysql.cj.jdbc.Driver"),
    POSTGRESQL("postgres:16", "org.postgresql.Driver"),
    REDIS("redis:7-alpine", null),
    MONGODB("mongo:7", "com.mongodb.MongoClient"),
    KAFKA("confluentinc/cp-kafka:7.5.0", null),
    ELASTICSEARCH("elasticsearch:8.11.0", null);

    private final String defaultImage;
    private final String driverClassName;

    ContainerType(String defaultImage, String driverClassName) {
        this.defaultImage = defaultImage;
        this.driverClassName = driverClassName;
    }

    public boolean isSqlDatabase() {
        return this == MARIADB || this == MYSQL || this == POSTGRESQL;
    }

    public String createJdbcUrl(String host, Integer port, String database) {
        if (!isSqlDatabase()) {
            throw new UnsupportedOperationException("JDBC URL not supported for " + this);
        }
        
        String dbName = database != null ? database : "test";
        return switch (this) {
            case MARIADB -> String.format("jdbc:mariadb://%s:%d/%s", host, port, dbName);
            case MYSQL -> String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
            case POSTGRESQL -> String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
            default -> throw new UnsupportedOperationException("Unsupported database type: " + this);
        };
    }
}