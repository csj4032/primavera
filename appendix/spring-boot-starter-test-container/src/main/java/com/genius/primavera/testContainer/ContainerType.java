package com.genius.primavera.testContainer;

public enum ContainerType {

    MARIADB("mariadb:11.4.7", 3306),
    REDIS("redis:7-alpine", 6380),
    KAFKA("confluentinc/cp-kafka:latest", 9092),
    POSTGRESQL("postgres:15-alpine", 5432);

    private final String dockerImage;
    private final int defaultPort;

    ContainerType(String dockerImage, int defaultPort) {
        this.dockerImage = dockerImage;
        this.defaultPort = defaultPort;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public int getDefaultPort() {
        return defaultPort;
    }
}