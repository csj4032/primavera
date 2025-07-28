package com.genius.primavera.testContainer;

import lombok.Getter;

@Getter
public enum ContainerType {
    ELASTICSEARCH("elasticsearch"),
    KAFKA("kafka"),
    MARIADB("mariadb"),
    MONGODB("mongodb"),
    MYSQL("mysql"),
    POSTGRESQL("postgresql"),
    REDIS("redis"),
    VAULT("vault");

    private final String type;

    ContainerType(String type) {
        this.type = type;
    }

}
