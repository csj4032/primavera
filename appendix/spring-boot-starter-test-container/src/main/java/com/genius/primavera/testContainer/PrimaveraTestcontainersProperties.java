package com.genius.primavera.testContainer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

//@Getter
//@Setter
//@Component
//@ConfigurationProperties(prefix = "primavera.testcontainers")
public class PrimaveraTestcontainersProperties {

    private Mariadb mariadb = new Mariadb();
    private Redis redis = new Redis();
    private PostgreSQL postgreSQL = new PostgreSQL();
    private Kafka kafka = new Kafka();

    @Getter
    @Setter
    public static class Mariadb {
        private String image = "mariadb:10.6";
        private String databaseName = "primavera_basic_test";
        private String username = "primavera";
        private String password = "testpass";
        private String initScript;
    }

    @Getter
    @Setter
    public static class Redis {
        private String image = "redis:6-alpine";
        private int port = 6379;
    }

    @Getter
    @Setter
    public static class PostgreSQL {
        private String image = "postgres:14";
        private String databaseName = "primavera";
        private String username = "primavera";
        private String password = "primavera";
        private String initScript;
        private Duration startupTimeout = Duration.ofMinutes(2);
    }

    @Getter
    @Setter
    public static class Kafka {
        private String image = "confluentinc/cp-kafka:latest";
        private String bootstrapServers = "localhost:9092";
        private String zookeeperConnect = "localhost:2181";
        private String advertisedListeners = "localhost:9092";
        private String listenerSecurityProtocolMap = "PLAINTEXT:PLAINTEXT";
        private Duration startupTimeout = Duration.ofMinutes(2);
        private Map<String, String> additionalProperties = new HashMap<>();
    }
}